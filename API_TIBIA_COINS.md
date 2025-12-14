# API Tibia Coins - Rei dos Coins Proxy

Esta API faz proxy seguro para a API do Rei dos Coins, protegendo o token secreto.

## Endpoints

### GET `/api/tibia-coins/price`

Busca o preço dos Tibia Coins por quantidade.

**Query Parameters:**
- `quantity` (Integer, obrigatório): Quantidade de Tibia Coins

**Exemplo:**
```bash
curl "http://www.tibiacacau.com.br/api/tibia-coins/price?quantity=75"
```

**Response:**
```json
{
  "price": 2.50,
  "quantity": 75,
  "total": 187.50
}
```

### POST `/api/tibia-coins/price`

Busca o preço dos Tibia Coins por quantidade (método POST).

**Request Body:**
```json
{
  "quantity": 75
}
```

**Exemplo:**
```bash
curl -X POST "http://www.tibiacacau.com.br/api/tibia-coins/price" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 75}'
```

**Response:**
```json
{
  "price": 2.50,
  "quantity": 75,
  "total": 187.50
}
```

## Configuração

As configurações estão no `application.yml`:

```yaml
reidoscoins:
    api:
        url: ${REIDOSCOINS_API_URL:https://www.reidoscoins.com.br/index.php}
        token: ${REIDOSCOINS_API_TOKEN:NBMRJK348THG3W7TRY2GHBV38245YR1291}
```

## Variáveis de Ambiente (Produção)

Para deploy em produção, configure as variáveis de ambiente:

```bash
REIDOSCOINS_API_URL=https://www.reidoscoins.com.br/index.php
REIDOSCOINS_API_TOKEN=seu_token_secreto_aqui
```

## Uso no Frontend (Angular)

```typescript
import { HttpClient } from '@angular/common/http';

constructor(private http: HttpClient) {}

getTibiaCoinPrice(quantity: number) {
  return this.http.get(`http://www.tibiacacau.com.br/api/tibia-coins/price?quantity=${quantity}`);
}
```

## Segurança

✅ **O token da API do Rei dos Coins nunca é exposto no frontend**
✅ **CORS configurado para aceitar requisições apenas de domínios permitidos**
✅ **Token pode ser configurado via variável de ambiente**
