// Cliente MCP mínimo por STDIO: lanza cada JAR, hace initialize, tools/list y llama tools.
import { spawn } from 'node:child_process';
import { appendFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const LOG = resolve('test_log.txt');
writeFileSync(LOG, `==== ${new Date().toISOString()}\n`);
const log = (s) => { appendFileSync(LOG, s + '\n'); console.log(s); };

function startServer(jar, env = {}) {
  const proc = spawn('java', ['-jar', jar], { env: { ...process.env, ...env } });
  let buf = '';
  const pending = new Map();
  let nextId = 1;
  proc.stdout.on('data', (d) => {
    buf += d.toString();
    let i;
    while ((i = buf.indexOf('\n')) >= 0) {
      const line = buf.slice(0, i).trim();
      buf = buf.slice(i + 1);
      if (!line) continue;
      let msg;
      try { msg = JSON.parse(line); } catch { log('!! stdout no-JSON (rompe el protocolo): ' + line); continue; }
      if (msg.id && pending.has(msg.id)) { pending.get(msg.id)(msg); pending.delete(msg.id); }
    }
  });
  proc.stderr.on('data', (d) => log('[stderr] ' + d.toString().trim().slice(0, 500)));
  const send = (method, params, timeout = 60000) => new Promise((res, rej) => {
    const id = nextId++;
    pending.set(id, res);
    proc.stdin.write(JSON.stringify({ jsonrpc: '2.0', id, method, params }) + '\n');
    setTimeout(() => { if (pending.has(id)) { pending.delete(id); rej(new Error('timeout ' + method)); } }, timeout);
  });
  const notify = (method, params) => proc.stdin.write(JSON.stringify({ jsonrpc: '2.0', method, params }) + '\n');
  return { proc, send, notify };
}

async function session(name, jar, calls, env) {
  log(`\n######## ${name}`);
  const s = startServer(jar, env);
  try {
    const init = await s.send('initialize', {
      protocolVersion: '2024-11-05', capabilities: {}, clientInfo: { name: 'lab-tester', version: '1.0' } }, 90000);
    log('initialize -> ' + JSON.stringify(init.result?.serverInfo ?? init.error));
    s.notify('notifications/initialized', {});
    const list = await s.send('tools/list', {});
    log('tools/list -> ' + (list.result?.tools ?? []).map((t) => t.name).join(', '));
    const ctx = {};
    for (const [tool, argsFn, key] of calls) {
      const args = typeof argsFn === 'function' ? argsFn(ctx) : argsFn;
      const r = await s.send('tools/call', { name: tool, arguments: args });
      const text = r.result?.content?.map((c) => c.text).join('') ?? JSON.stringify(r.error);
      log(`${r.result?.isError ? 'ERR' : 'OK '} ${tool}(${JSON.stringify(args)})\n    -> ${text.slice(0, 600)}`);
      ctx[key ?? tool] = (() => { try { return JSON.parse(text); } catch { return text; } })();
    }
  } catch (e) {
    log('FALLO: ' + e.message);
  } finally {
    s.proc.kill();
  }
}

await session('mcp-server-tdea', 'mcp-server-tdea/target/mcp-server-tdea-0.0.1-SNAPSHOT.jar', [
  ['addItem', { name: 'manzana', quantity: 3, category: 'fruta', price: 1200, brand: 'Campo', description: 'roja' }],
  ['addItem', { name: 'leche', quantity: 2, category: 'lácteos', price: 4500, brand: 'Colanta', description: 'entera' }],
  ['getItems', {}],
  ['removeItem', { name: 'manzana', quantity: 1 }],
  ['getItems', {}],
]);

if (process.env.SKIP_BANCO === '1') {
  log('\n(banco omitido: no se encontró MySQL)');
} else {
  await session('mcp-server-banco', 'mcp-server-banco/target/mcp-server-banco-0.0.1-SNAPSHOT.jar', [
    ['listAccounts', {}],
    ['createAccount', { ownerName: 'Ana Ruiz', ownerDocument: '43123456', accountType: 'SAVINGS', initialDeposit: 300000 }, 'ana'],
    ['createAccount', { ownerName: 'Pedro Díaz', ownerDocument: '71555444', accountType: 'CHECKING', initialDeposit: 0 }, 'pedro'],
    ['deposit', (c) => ({ accountNumber: c.pedro?.accountNumber, amount: 150000, description: 'nómina' })],
    ['transfer', (c) => ({ fromAccount: c.pedro?.accountNumber, toAccount: c.ana?.accountNumber, amount: 50000, description: 'prueba' })],
    ['transfer', (c) => ({ fromAccount: c.ana?.accountNumber, toAccount: c.pedro?.accountNumber, amount: 999999999, description: 'sin fondos' })],
    ['getTransactions', (c) => ({ accountNumber: c.pedro?.accountNumber, limit: 10 })],
    ['requestLoan', (c) => ({ accountNumber: c.pedro?.accountNumber, amount: 10000000, termMonths: 24, monthlyIncome: 2000000, purpose: 'moto' }), 'loan'],
    ['disburseLoan', (c) => ({ loanId: c.loan?.loanId })],
    ['disburseLoan', (c) => ({ loanId: c.loan?.loanId })],
    ['requestLoan', (c) => ({ accountNumber: c.pedro?.accountNumber, amount: 10000000, termMonths: 24, monthlyIncome: 1000000 })],
    ['getAccount', (c) => ({ accountNumber: c.pedro?.accountNumber })],
  ], {});
}
log('\n==== FIN PRUEBAS');
