# Laboratorio MCP – Spring AI + Claude Desktop (TdeA 2026)

Dos proyectos Spring Boot 3.5.3 + Spring AI 1.0.0 con transporte **STDIO**:

| Carpeta | Qué es |
|---|---|
| `mcp-server-tdea/` | Parte guiada: lista de compras en memoria (`addItem`, `getItems`, `removeItem`). |
| `mcp-server-banco/` | Ejercicio: servicios bancarios sobre MySQL (cuentas, consignaciones, retiros, transferencias, créditos). |

## Requisitos
Java 17+, Maven 3.6+, MySQL 8 (solo para el banco), Claude Desktop y Node.js (para MCP Inspector).

## 1. Compilar
```bash
cd mcp-server-tdea  && mvn clean package
cd ../mcp-server-banco && mvn clean package      # las pruebas usan H2, no necesitan MySQL
```
Genera `target/*-0.0.1-SNAPSHOT.jar` en cada proyecto.

## 2. Base de datos (banco)
Basta con que MySQL esté encendido: la BD `banco_mcp` y las tablas (`accounts`, `transactions`, `loan_requests`) se crean solas.
Credenciales por variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
En el primer arranque se crean dos cuentas de prueba (Juan Pérez y María Gómez).

## 3. Conectar a Claude Desktop
1. Abrir `%APPDATA%\Claude\claude_desktop_config.json` (Windows) o `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS).
2. Copiar el contenido de `claude_desktop_config.example.json`, cambiando rutas de los JAR y la clave de MySQL.
3. Cerrar Claude Desktop por completo (también desde la bandeja del sistema) y abrirlo de nuevo.
4. En el ícono de herramientas deben aparecer los servidores y sus tools.

## 4. Probar
Con MCP Inspector (sin Claude):
```bash
npx @modelcontextprotocol/inspector java -jar mcp-server-banco/target/mcp-server-banco-0.0.1-SNAPSHOT.jar
```
Para el banco, define antes `DB_PASSWORD` en la terminal (`$env:DB_PASSWORD="..."` en PowerShell).

Prompts de ejemplo en Claude Desktop:
- "Lista las cuentas del banco."
- "Abre una cuenta de ahorros a nombre de Ana Ruiz, cédula 43123456, con 300.000 pesos."
- "Consigna 150.000 en la cuenta de Ana."
- "Transfiere 50.000 de la cuenta de Juan Pérez a la de Ana."
- "Muéstrame el extracto de Ana."
- "Ana gana 2.000.000 al mes y quiere un crédito de 10 millones a 24 meses." → aprobado (cuota 499.241,02)
- "Desembolsa ese crédito."

## Tools del banco
| Tool | Descripción |
|---|---|
| `createAccount` | Abre cuenta SAVINGS/CHECKING con depósito inicial opcional |
| `getAccount` / `listAccounts` | Consulta de cuenta y saldo |
| `deposit` / `withdraw` | Consignación y retiro |
| `transfer` | Transferencia atómica (`@Transactional` + bloqueo `SELECT … FOR UPDATE`) |
| `getTransactions` | Extracto (máx. 50 movimientos) |
| `requestLoan` | Solicitud evaluada al instante: cuota (sistema francés, 18 % anual) ≤ 40 % del ingreso |
| `disburseLoan` | Abona un crédito aprobado; no permite doble desembolso |
| `listLoans` | Créditos de una cuenta |

Las reglas (tasa, monto máximo, % de cuota) se cambian en `application.properties` (`bank.loan.*`).

## Problemas comunes
- **El servidor no aparece en Claude:** ruta del JAR mal escrita (en JSON las `\` van dobles) o Claude no se reinició del todo.
- **Error al conectar:** MySQL apagado o clave incorrecta. Revisar `~/mcp-logs/mcp-server-banco.log` (en Windows `C:\Users\TU_USUARIO\mcp-logs`).
- **Mensajes JSON corruptos:** algo se imprimió por consola. En STDIO nunca usar `System.out.println` ni `spring.jpa.show-sql=true`.
