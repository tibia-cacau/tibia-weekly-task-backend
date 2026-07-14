# 🚀 Deploy Instructions (Linux Server)

## Problema: Line Endings Windows no Linux

Os arquivos criados no Windows têm line endings `CRLF` (`\r\n`), mas o Linux precisa de `LF` (`\n`).

### Solução Rápida

No servidor Linux, execute:

```bash
# 1. Upload todos os arquivos para /home/tibiacacau/www/
# 2. Entre no diretório
cd /home/tibiacacau/www/

# 3. Faça o script fix-line-endings.sh executável
chmod +x fix-line-endings.sh

# 4. Execute o script para corrigir todos os arquivos
./fix-line-endings.sh

# 5. Pare qualquer processo rodando
./stop.sh

# 6. Inicie o backend
./start.sh

# 7. Verifique os logs
tail -f backend.log
```

### Verificar se funcionou

Você deve ver no log:

```
Started WeeklyTasksApplication in X.XX seconds
```

Se aparecer erro de conexão MySQL, verifique o `.env`:

```bash
cat .env
```

Certifique-se que tem aspas duplas em todas as variáveis:

```bash
DATASOURCE_URL="jdbc:mysql://..."
DATASOURCE_PASSWORD="Fer6j4891274"
```

### Scripts Disponíveis

- `./start.sh` - Inicia o backend
- `./stop.sh` - Para o backend
- `./restart.sh` - Reinicia o backend
- `./test-env.sh` - Testa se o .env está sendo carregado corretamente

### Testar o Backend

```bash
# Verificar se está rodando
ps aux | grep java

# Testar endpoint
curl http://localhost:8080/api/items

# Ver logs em tempo real
tail -f backend.log
```

### Troubleshooting

**Erro: `$'\r': command not found`**

- Execute: `./fix-line-endings.sh`

**Erro: `Permission denied`**

- Execute: `chmod +x *.sh`

**Erro: `Access denied for user 'tibiacacau'@...`**

- Verifique se o `.env` tem aspas duplas
- Execute: `./test-env.sh` para testar variáveis

**Backend não inicia**

- Verifique logs: `tail -f backend.log`
- Verifique Java: `java -version`
- Verifique memória: `free -h`
