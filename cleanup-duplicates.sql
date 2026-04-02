-- ============================================================
-- LIMPEZA DE DUPLICATAS NA TABELA MONSTERS
-- Execute as queries em ordem e verifique cada resultado
-- antes de prosseguir para o próximo passo
-- ============================================================

-- PASSO 1: Quantos duplicados existem?
SELECT COUNT(*) AS duplicatas_a_remover
FROM monsters m1
WHERE m1.id > 798
  AND EXISTS (
    SELECT 1 FROM monsters m2
    WHERE m2.name = m1.name AND m2.id <= 798
  );

-- ============================================================

-- PASSO 2: Ver quais registros serão removidos
SELECT m1.id, m1.name, m1.hitpoints, m1.armor, m1.mitigation
FROM monsters m1
WHERE m1.id > 798
  AND EXISTS (
    SELECT 1 FROM monsters m2
    WHERE m2.name = m1.name AND m2.id <= 798
  )
ORDER BY m1.name;

-- ============================================================

-- PASSO 3: Ver registros com id > 798 que NÃO têm duplicata (devem ser MANTIDOS)
SELECT id, name, hitpoints, armor, mitigation
FROM monsters
WHERE id > 798
  AND name NOT IN (SELECT name FROM monsters WHERE id <= 798)
ORDER BY name;

-- ============================================================

-- PASSO 4: Criar backup antes de deletar
CREATE TABLE monsters_backup AS SELECT * FROM monsters;

-- ============================================================

-- PASSO 5: Deletar os duplicados (só execute após confirmar passos 1-3)
DELETE FROM monsters
WHERE id > 798
  AND name IN (SELECT name FROM monsters_backup WHERE id <= 798);

-- ============================================================

-- PASSO 6: Verificar resultado final
SELECT COUNT(*) AS total_apos_limpeza FROM monsters;

-- ============================================================

-- PASSO 7: Resetar auto_increment (opcional)
ALTER TABLE monsters AUTO_INCREMENT = 799;
