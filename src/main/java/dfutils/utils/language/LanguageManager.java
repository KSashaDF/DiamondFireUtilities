package dfutils.utils.language;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class manages the loading of language files and
 * also manages the getting of translation components.
 */
public class LanguageManager {
    
    private static String loadedLanguageName = "";
    private static JsonObject languageData;
    private static String suppressLanguage = "";
    
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    private static final LanguageManager instance = new LanguageManager();
    private static final String VARIABLE_CODE = "%VAR%";
    private static final TextComponentString MISSING_FILE = new TextComponentString("MISSING LANGUAGE FILE");
    private static final TextComponentString MISSING_TRANSLATION = new TextComponentString("MISSING TRANSLATION");
    private static final TextComponentString INVALID_TRANSLATION = new TextComponentString("INVALID TRANSLATION");
    
    private LanguageManager() {}
    
    public static ITextComponent getMessage(String translationKey) {
        
        //If the currently selected language does not equal the loaded language, reload the language file.
        //Also checks if the currently selected language is not suppressed.
        if (!loadedLanguageName.equals(minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode()) && !suppressLanguage.equals(minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode())) {
            instance.loadLanguageFile(minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode());
        }
        
        if (languageData == null) {
            return MISSING_FILE;
        }
        
        String[] translationPath = translationKey.split("[.]");
        int pathPosition = -1;
        JsonObject searchData = languageData;
        
        while (true) {
            pathPosition++;
            if (pathPosition >= translationPath.length) {
                return MISSING_TRANSLATION;
            }
            
            if (searchData.has(translationPath[pathPosition])) {
                JsonElement nextNbtTag = searchData.get(translationPath[pathPosition]);
                
                if (TranslationComponentHelper.isTranslationComponent(nextNbtTag)) {
                    return TranslationComponentHelper.parseTextComponent(nextNbtTag);
                } else if (nextNbtTag.isJsonObject()) {
                    searchData = (JsonObject) nextNbtTag;
                } else {
                    return INVALID_TRANSLATION;
                }
                
            } else {
                return MISSING_TRANSLATION;
            }
        }
    }
    
    /**
     * This method gets the specifies translation component,
     * however this method also parses %VAR% variable codes.
     */
    public static ITextComponent getMessage(String translationKey, @Nonnull String... variables) {
        ITextComponent translationTextComponent = getMessage(translationKey);
        String translationComponent;
        if (translationTextComponent instanceof TextComponentString) {
            translationComponent = "{\"text\":\"" + translationTextComponent.getUnformattedComponentText() + "\"}";
        } else {
            translationComponent = translationTextComponent.getUnformattedComponentText();
        }
        
        if (translationComponent.contains(VARIABLE_CODE)) {
            for (int i = 0; i < variables.length && translationComponent.contains(VARIABLE_CODE); i++) {
                translationComponent = translationComponent.replaceFirst(VARIABLE_CODE, variables[i]);
            }
        }
        
        return ITextComponent.Serializer.jsonToComponent(translationComponent);
    }
    
    
    private void loadLanguageFile(String languageName) {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/assets/dfutils/lang/" + languageName + ".json")) {
            languageData = new JsonParser().parse(IOUtils.toString(inputStream, Charsets.UTF_8)).getAsJsonObject();
            loadedLanguageName = languageName;
        } catch (IOException exception) {
            languageData = null;
            suppressLanguage = languageName;
            
            if (!languageName.equals("en_us")) {
                minecraft.player.sendMessage(new TextComponentString("§cUh oh! An error occurred while trying to load language data, defaulting to en_us."));
                loadLanguageFile("en_us");
            }
        } catch (NullPointerException exception) {
            //If an NPE Exception has occurred, it most likely means that the specified language file does not exist.
            languageData = null;
            suppressLanguage = languageName;
    
            if (!languageName.equals("en_us")) {
                minecraft.player.sendMessage(new TextComponentString("§cUh oh! Could not find the specified language, defaulting to en_us."));
                loadLanguageFile("en_us");
            }
        }
    }
}
