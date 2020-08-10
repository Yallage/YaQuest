package com.rabbitown.yaquest;

import com.rabbitown.yalib.locale.YLocale;
import com.rabbitown.yaquest.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * @author Yoooooory
 */
public class YaQuest extends JavaPlugin {

    public static HashMap<String, HashMap<String, String>> locale = new HashMap<>();

    @Override
    public void onEnable() {
        Logger.prefix = locale.get(new YLocale().getConsoleDefaultLanguage()).get("prefix");
        Logger.info("§aYaQuest v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        Logger.info("§aYaQuest v" + getDescription().getVersion() + " disabled!");
    }

    @Override
    public void onLoad() {
        loadConfig();
    }

    public void loadConfig() {
        saveDefaultConfig();
        saveResource("language/zh_CN.yml", false);
        YLocale locale = new YLocale();
        YaQuest.locale = locale.getMessageOfHashMap("plugins/YaQuest/language", true);
    }

}
