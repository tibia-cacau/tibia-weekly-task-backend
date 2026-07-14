# Monster Web Scraper - TibiaWiki

Web scraper para atualizar informações de monstros no banco de dados a partir do TibiaWiki.

## 🎯 Objetivo

Buscar e atualizar automaticamente:

- **Hit Points (HP)**
- **Armadura (Armor)**
- **Mitigação (Mitigation)**

## 📋 Monstros Incluídos

O scraper atualiza 102 monstros automaticamente, incluindo:

- **Kilmaresh:** Black Sphinx Acolyte, Cobra Scout, Cobra Vizier, etc.
- **Carnisylvans:** Hulking, Poisonous, Dark Carnisylvan
- **Raubritters:** Skirmisher, Marksman, Chastener
- **Orcs:** Orc, Orc Warrior, Orc Berserker, Orc Warlord, etc.
- **Trolls:** Troll, Frost Troll, Swamp Troll, Island Troll, etc.
- **Minotauros:** Minotaur, Minotaur Mage, Minotaur Guard, etc.
- **Elfos e Anões:** Elf, Elf Arcanist, Dwarf, Dwarf Geomancer, etc.
- **Outros:** Goblins, Chakoya, Corym, Piratas, etc.

Lista completa em: `MonsterScraperService.java` (constante `MONSTERS_TO_SCRAPE`)

## 🚀 Como Usar

### 1. Via Endpoint REST

Execute uma requisição POST para o endpoint:

```http
POST http://localhost:8080/api/calculator/scrape-monsters
```

**Exemplo com curl:**

```bash
curl -X POST http://localhost:8080/api/calculator/scrape-monsters
```

**Exemplo com Postman:**

- Method: POST
- URL: `http://localhost:8080/api/calculator/scrape-monsters`
- Body: (não necessário)

### 2. Resposta Esperada

```json
{
	"total": 102,
	"success": 95,
	"failed": 3,
	"notFoundInDb": 4,
	"failedMonsters": [
		"Minotaur Amazon (not found in DB)",
		"Crape Man (scraping failed)",
		"Orc Leader (error: timeout)"
	]
}
```

## 🔧 Detalhes Técnicos

### Tecnologias

- **Jsoup 1.17.2** - Parsing de HTML
- **Spring Boot** - Framework REST
- **JPA/Hibernate** - Persistência

### Funcionamento

1. **Conexão:** Acessa `https://www.tibiawiki.com.br/wiki/[Nome_do_Monstro]`
2. **Parsing:** Extrai dados da tabela HTML `<table class="infobox-main">`
3. **Busca no DB:** Localiza monstro pelo nome usando `searchByName()`
4. **Atualização:** Compara e atualiza apenas campos diferentes
5. **Delay:** Aguarda 1 segundo entre requisições

### Configurações

```java
TIBIAWIKI_BASE_URL = "https://www.tibiawiki.com.br/wiki/"
TIMEOUT_MS = 10000 // 10 segundos
DELAY_BETWEEN_REQUESTS_MS = 1000 // 1 segundo
```

### Extração de Dados

- **HP:** Busca por texto contendo "HP" → extrai números
- **Armor:** Busca por "Armadura" ou "Armor" → extrai números
- **Mitigation:** Busca por "Mitigação" ou "Mitigation" → extrai decimal com %

## ⚠️ Observações Importantes

1. **Performance:**
    - 102 monstros com delay de 1s = ~2 minutos de execução
    - Não execute paralelamente para evitar sobrecarga no TibiaWiki

2. **Monstros Não Encontrados:**
    - Alguns nomes podem estar diferentes no wiki (ex: "Mooh'tah" vs "Moohtah")
    - Verifique logs para ajustar nomes se necessário

3. **Falhas de Scraping:**
    - Timeout de conexão
    - Mudanças na estrutura HTML do TibiaWiki
    - Bloqueio por excesso de requisições

4. **Banco de Dados:**
    - Apenas atualiza monstros já existentes
    - Não cria novos registros
    - Use `searchByName()` para busca flexível (LIKE %nome%)

## 📊 Logs

O serviço gera logs detalhados:

```
INFO  - Scraping monster: Black Sphinx Acolyte
DEBUG - Fetching URL: https://www.tibiawiki.com.br/wiki/Black_Sphinx_Acolyte
DEBUG - Found HP: 8100 for Black Sphinx Acolyte
DEBUG - Found Armor: 82 for Black Sphinx Acolyte
DEBUG - Found Mitigation: 2.28% for Black Sphinx Acolyte
INFO  - Updating Black Sphinx Acolyte armor: null -> 82
INFO  - Successfully updated monster: Black Sphinx Acolyte
```

## 🛠️ Troubleshooting

### Problema: "Monster not found in database"

**Solução:** O nome no banco está diferente do TibiaWiki. Opções:

1. Ajustar nome no banco de dados
2. Ajustar nome na lista `MONSTERS_TO_SCRAPE`
3. Adicionar mapeamento de nomes alternativos

### Problema: "No infobox-main table found"

**Solução:** O TibiaWiki mudou a estrutura HTML. Verifique a página manualmente e atualize os seletores CSS em `scrapeMonster()`.

### Problema: Timeout errors

**Solução:**

1. Aumentar `TIMEOUT_MS`
2. Verificar conexão com internet
3. Tentar novamente mais tarde

## 📝 To-Do / Melhorias Futuras

- [ ] Adicionar extração de resistências elementais
- [ ] Suporte para scraping de experiência
- [ ] Cache de páginas para evitar re-scraping
- [ ] Retry automático em caso de falha
- [ ] Interface web para gerenciar scraping
- [ ] Agendamento automático (ex: semanal)
- [ ] Validação de dados extraídos
- [ ] Notificações de problemas

## 🔍 Exemplo de Uso Completo

```bash
# 1. Verificar monstros com armor NULL antes
curl http://localhost:8080/api/calculator/monsters | jq '.[] | select(.armor == null) | .name'

# 2. Executar scraping
curl -X POST http://localhost:8080/api/calculator/scrape-monsters

# 3. Verificar resultados
curl http://localhost:8080/api/calculator/monsters/106
```

## 📜 Licença

Parte do projeto Tibia Cacau.
