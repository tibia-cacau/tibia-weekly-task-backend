package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonsterScraperService {

    private final MonsterRepository monsterRepository;

    private static final String TIBIAWIKI_BASE_URL = "https://www.tibiawiki.com.br/wiki/";
    private static final int TIMEOUT_MS = 10000;
    private static final int DELAY_BETWEEN_REQUESTS_MS = 1000; // 1 segundo entre requisições

    /**
     * Lista de monstros para fazer scraping
     */
    private static final List<String> MONSTERS_TO_SCRAPE = Arrays.asList(
            "Glooth Brigand", "Cobra Scout", "Burning Gladiator", "Priestess of the Wild Sun",
            "Black Sphinx Acolyte", "Cobra Vizier", "Usurper Knight", "Usurper Archer",
            "Usurper Warlock", "Hulking Carnisylvan", "Poisonous Carnisylvan", "Dark Carnisylvan",
            "Raubritter Skirmisher", "Raubritter Marksman", "Raubritter Chastener", "Orc Warlord",
            "Orc Rider", "Orc", "Orc Shaman", "Orc Warrior", "Orc Berserker", "Troll",
            "Minotaur Mage", "Minotaur Archer", "Minotaur", "Minotaur Guard", "Orc Spearman",
            "Frost Troll", "Orc Leader", "Goblin", "Elf", "Elf Arcanist", "Elf Scout",
            "Dwarf Geomancer", "Dwarf", "Dwarf Guard", "Dwarf Soldier", "Swamp Troll",
            "Dworc Voodoomaster", "Dworc Fleshhunter", "Dworc Venomsniper", "Island Troll",
            "Chakoya Tribewarden", "Chakoya Toolshaper", "Chakoya Windcaller", "Goblin Leader",
            "Dwarf Henchman", "Troll Champion", "Grynch Clan Goblin", "Goblin Assassin",
            "Goblin Scavenger", "Furious Troll", "Troll Legionnaire", "Orc Marauder",
            "Firestarter", "Elf Overseer", "Troll Guard", "Enslaved Dwarf", "Lost Berserker",
            "Corym Charlatan", "Corym Skirmisher", "Corym Vanguard", "Little Corym Charlatan",
            "Lost Husher", "Lost Basher", "Lost Thrower", "Moohtant", "Minotaur Amazon",
            "Execowtioner", "Mooh'tah Warrior", "Minotaur Hunter", "Worm Priestess",
            "Muglex Clan Footman", "Muglex Clan Assassin", "Minotaur Invader", "Broken Shaper",
            "Twisted Shaper", "Shaper Matriarch", "Misguided Bully", "Misguided Thief",
            "Barkless Devotee", "Barkless Fanatic", "Orc Cultist", "Orc Cult Priest",
            "Orc Cult Inquisitor", "Orc Cult Fanatic", "Orc Cult Minion", "Minotaur Cult Follower",
            "Minotaur Cult Prophet", "Minotaur Cult Zealot", "Lost Exile", "Crazed Winter Vanguard",
            "Crazed Winter Rearguard", "Crazed Summer Vanguard", "Crazed Summer Rearguard",
            "Soul-Broken Harbinger", "Insane Siren", "Pirat Cutthroat", "Pirat Scoundrel",
            "Pirat Bombardier", "Pirat Mate", "Crape Man");

    @Transactional
    public Map<String, Object> scrapeMonsters() {
        log.info("Starting monster scraping from TibiaWiki");

        int successCount = 0;
        int failCount = 0;
        int notFoundCount = 0;
        List<String> failedMonsters = new ArrayList<>();

        for (String monsterName : MONSTERS_TO_SCRAPE) {
            try {
                log.info("Scraping monster: {}", monsterName);

                MonsterData data = scrapeMonster(monsterName);

                if (data != null) {
                    // Buscar monstro no banco de dados pelo nome
                    List<Monster> monsters = monsterRepository.searchByName(monsterName);

                    if (!monsters.isEmpty()) {
                        Monster monster = monsters.get(0); // Pega o primeiro resultado

                        // Atualizar dados
                        boolean updated = false;

                        if (data.getHitpoints() != null && !data.getHitpoints().equals(monster.getHitpoints())) {
                            log.info("Updating {} hitpoints: {} -> {}", monsterName, monster.getHitpoints(),
                                    data.getHitpoints());
                            monster.setHitpoints(data.getHitpoints());
                            updated = true;
                        }

                        if (data.getArmor() != null && !data.getArmor().equals(monster.getArmor())) {
                            log.info("Updating {} armor: {} -> {}", monsterName, monster.getArmor(), data.getArmor());
                            monster.setArmor(data.getArmor());
                            updated = true;
                        }

                        if (data.getMitigation() != null && (monster.getMitigation() == null ||
                                Double.compare(data.getMitigation().doubleValue(), monster.getMitigation()) != 0)) {
                            log.info("Updating {} mitigation: {} -> {}", monsterName, monster.getMitigation(),
                                    data.getMitigation());
                            monster.setMitigation(data.getMitigation().doubleValue());
                            updated = true;
                        }

                        if (updated) {
                            monsterRepository.save(monster);
                            log.info("Successfully updated monster: {}", monsterName);
                            successCount++;
                        } else {
                            log.info("No updates needed for monster: {}", monsterName);
                        }
                    } else {
                        log.warn("Monster not found in database: {}", monsterName);
                        notFoundCount++;
                        failedMonsters.add(monsterName + " (not found in DB)");
                    }
                } else {
                    log.warn("Failed to scrape monster: {}", monsterName);
                    failCount++;
                    failedMonsters.add(monsterName + " (scraping failed)");
                }

                // Delay entre requisições para não sobrecarregar o servidor
                Thread.sleep(DELAY_BETWEEN_REQUESTS_MS);

            } catch (Exception e) {
                log.error("Error scraping monster {}: {}", monsterName, e.getMessage(), e);
                failCount++;
                failedMonsters.add(monsterName + " (error: " + e.getMessage() + ")");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", MONSTERS_TO_SCRAPE.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("notFoundInDb", notFoundCount);
        result.put("failedMonsters", failedMonsters);

        log.info("Scraping completed. Success: {}, Failed: {}, Not found in DB: {}",
                successCount, failCount, notFoundCount);

        return result;
    }

    private MonsterData scrapeMonster(String monsterName) throws IOException {
        // Converter nome para formato URL (espaços -> underscores)
        String urlName = monsterName.replace(" ", "_");
        String url = TIBIAWIKI_BASE_URL + urlName;

        log.debug("Fetching URL: {}", url);

        Document doc = Jsoup.connect(url)
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get();

        // Buscar tabela com classe "infobox-main"
        Elements infoboxes = doc.select("table.infobox-main");

        if (infoboxes.isEmpty()) {
            log.warn("No infobox-main table found for monster: {}", monsterName);
            return null;
        }

        Element infobox = infoboxes.first();
        MonsterData data = new MonsterData();

        // Buscar por todas as células da tabela
        Elements cells = infobox.select("td");

        for (Element cell : cells) {
            String text = cell.text();

            // Buscar HP
            if (text.contains("HP") && data.getHitpoints() == null) {
                String hpText = text.replaceAll("[^0-9]", ""); // Remove tudo exceto números
                if (!hpText.isEmpty()) {
                    try {
                        data.setHitpoints(Integer.parseInt(hpText));
                        log.debug("Found HP: {} for {}", data.getHitpoints(), monsterName);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse HP from: {}", text);
                    }
                }
            }

            // Buscar Armor
            if (text.contains("Armadura") || text.contains("Armor")) {
                String armorText = text.replaceAll("[^0-9]", "");
                if (!armorText.isEmpty()) {
                    try {
                        data.setArmor(Integer.parseInt(armorText));
                        log.debug("Found Armor: {} for {}", data.getArmor(), monsterName);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse Armor from: {}", text);
                    }
                }
            }

            // Buscar Mitigation
            if (text.contains("Mitigação") || text.contains("Mitigation")) {
                // Extrair número decimal (ex: "2.28% de Mitigação" -> "2.28")
                String mitigationText = text.replaceAll("[^0-9.]", "");
                if (!mitigationText.isEmpty()) {
                    try {
                        data.setMitigation(new BigDecimal(mitigationText));
                        log.debug("Found Mitigation: {}% for {}", data.getMitigation(), monsterName);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse Mitigation from: {}", text);
                    }
                }
            }
        }

        if (data.getHitpoints() == null && data.getArmor() == null && data.getMitigation() == null) {
            log.warn("No data extracted for monster: {}", monsterName);
            return null;
        }

        return data;
    }

    /**
     * Classe interna para armazenar dados temporários do scraping
     */
    private static class MonsterData {
        private Integer hitpoints;
        private Integer armor;
        private BigDecimal mitigation;

        public Integer getHitpoints() {
            return hitpoints;
        }

        public void setHitpoints(Integer hitpoints) {
            this.hitpoints = hitpoints;
        }

        public Integer getArmor() {
            return armor;
        }

        public void setArmor(Integer armor) {
            this.armor = armor;
        }

        public BigDecimal getMitigation() {
            return mitigation;
        }

        public void setMitigation(BigDecimal mitigation) {
            this.mitigation = mitigation;
        }
    }
}
