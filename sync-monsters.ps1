#!/usr/bin/env pwsh
# Script para sincronizar monstros manualmente no banco de dados

Write-Host "🔄 Sincronizando monstros da API Tibia Draptor..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/calculator/sync-monsters" -Method POST -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✅ Sincronização concluída com sucesso!" -ForegroundColor Green
        Write-Host "📊 Total de monstros: $($response.totalMonsters)" -ForegroundColor Green
    } else {
        Write-Host "❌ Erro na sincronização: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erro ao conectar com o backend:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "`n💡 Certifique-se de que o backend está rodando em http://localhost:8080" -ForegroundColor Yellow
}
