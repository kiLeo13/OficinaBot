package ofc.bot.handlers.economy;

import ofc.bot.handlers.requests.Method;
import ofc.bot.internal.data.BotData;
import ofc.bot.handlers.requests.RequesterManager;
import com.google.gson.Gson;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UEconomyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UEconomyManager.class);
    private static final String TOKEN = BotData.get("unbelievaboat.token");
    private static final String MEDIA_TYPE = "application/json";
    private static final String EMPTY_JSON = "{}";
    private static final Gson GSON = new Gson();

    private UEconomyManager() {}

    @NotNull
    public static Balance getBalance(long guildId, long userId) {
        String json = fetchBalance(userId, guildId);

        return GSON.fromJson(json, Balance.class)
                .setGuildId(guildId);
    }

    @Nullable
    public static Balance updateBalance(long guildId, long userId, long cash, long bank, String reason) {
        try {
            String response = pushNewBalance(guildId, userId, cash, bank, true, reason);

            return GSON.fromJson(response, Balance.class)
                    .setGuildId(guildId);

        } catch (IOException e) {
            LOGGER.error("Could not update balance of user '{}'", userId, e);
            return null;
        }
    }

    @Nullable
    public static Balance setBalance(long guildId, long userId, long cash, long bank, String reason) {
        try {
            String response = pushNewBalance(guildId, userId, cash, bank, false, reason);

            return GSON.fromJson(response, Balance.class)
                    .setGuildId(guildId);

        } catch (IOException e) {
            LOGGER.error("Could not set balance of user '{}'", userId, e);
            return null;
        }
    }

    @Nullable
    public static Balance resetBalance(long guildId, long userId) {
        return setBalance(guildId, userId, 0, 0, null);
    }

    private static String fetchBalance(long userId, long guildId) {
        String url = "https://unbelievaboat.com/api/v1/guilds/" + guildId + "/users/" + userId;
        String json = makeRequest(url, Method.GET, null);

        return json == null || json.isBlank() ? EMPTY_JSON : json;
    }

    private static String pushNewBalance(long guildId, long userId, long cash, long bank, boolean isUpdate, String reason) throws IOException {
        String url = "https://unbelievaboat.com/api/v1/guilds/" + guildId + "/users/" + userId;
        Method method = isUpdate ? Method.PATCH : Method.PUT;
        DataObject requestBody = DataObject.empty()
                .put("cash", cash > Integer.MAX_VALUE ? "Infinity" : cash)
                .put("bank", bank > Integer.MAX_VALUE ? "Infinity" : bank);

        if (reason != null && !reason.isBlank())
            requestBody.put("reason", reason);

        return makeRequest(url, method, requestBody);
    }

    private static String makeRequest(String url, Method method, DataObject body) {
        return RequesterManager.newCall(url)
                .setMethod(method)
                .addHeader("Authorization", TOKEN)
                .addHeader("accept", MEDIA_TYPE)
                .setMediaType(MEDIA_TYPE)
                .setBody(body)
                .get(String::new);
    }
}