package ofc.bot.commands.relationships.marriages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ofc.bot.commands.relationships.MarryCommand;
import ofc.bot.domain.entity.enums.ExclusionType;
import ofc.bot.domain.sqlite.repository.MarriageRepository;
import ofc.bot.domain.sqlite.repository.UserExclusionRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.time.Instant;
import java.util.List;

@DiscordCommand(name = "marriage help", description = "Mostra informações gerais sobre o recurso de casamento.")
public class MarriageHelpCommand extends SlashSubcommand {
    private final UserExclusionRepository exclRepo;
    private final MarriageRepository marrRepo;

    public MarriageHelpCommand(UserExclusionRepository exclRepo, MarriageRepository marrRepo) {
        this.exclRepo = exclRepo;
        this.marrRepo = marrRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUser().getIdLong();
        MessageEmbed embed = embed(userId);

        return ctx.create(true)
                .setEmbeds(embed)
                .send();
    }

    private MessageEmbed embed(long userId) {
        EmbedBuilder builder = new EmbedBuilder();
        String description = getDesc(userId);

        return builder
                .setTitle("Casamentos 💍")
                .setDescription(description)
                .appendDescription("\nPara mais dúvidas: <#677986496065699881>.")
                .setTimestamp(Instant.now())
                .setColor(Bot.Colors.DEFAULT)
                .build();
    }

    private String getDesc(long userId) {
        int affectedMarriageCount = findAffectedCount(userId);
        String initialCost = Bot.fmtNum(MarryCommand.INITIAL_MARRIAGE_COST);
        String userDailyCost = Bot.fmtNum((long) MarryCommand.DAILY_COST * affectedMarriageCount);

        return String.format("""
                Não é de hoje que os casamentos vêm se tornando um dos recursos mais \
                utilizados do bot. Para que você entenda de maneira clara \
                esse módulo em um todo, este guia foi criado.
                
                > Custo inicial: `$%s`.
                > Custo diário: `$%s` (para você).
                
                ## 🔒 Limitações
                Limite membros Geral: `%d casamentos`.
                Limite membros Salada: `%d casamentos`.
                Membros em Manage Server: `sem limite`.
                
                Se casar com membros em `Manage Server` não cobrará o valor inicial \
                e nem a taxa diária por casamento, porém contarão no \
                seu limite de casamentos.
                
                ## 💸 Taxa Diária
                A taxa diária funciona de maneira [__atômica__](<https://en.wikipedia.org/wiki/ACID>), \
                isso significa que, ou todos os membros são cobrados/divorciados de acordo, ou, em caso de erro, \
                todos falham, não existe meio termo.
                Se um usuário não tem dinheiro para se manter casado com todos que ele se comprometeu, então \
                às <t:946692000:t> ele será divorciado em ordem por criação (do casamento mais recente até o mais antigo) \
                até que ele tenha o saldo suficiente (ou perca todos os casamentos).
                
                ### 🤔 E qual a lógica disso?
                Vamos usar este exemplo na respectiva ordem:
                
                - Pedro se casou com Maria.
                - Pedro se casou com Arthur.
                - Pedro se casou com Carlos.
                - Pedro se casou com Rita.
                
                Imagine que Maria não tem dinheiro para sustentar seu casamento com Pedro; \
                Pedro tem dinheiro para manter apenas 3 casamentos.
                Nesse caso, Maria poderia ser divorciada de Pedro (ficando ele com 3 casamentos) \
                e... ótimo, Pedro agora pode sustentar seus 3 casamentos. Mas bem, isso não soa justo, \
                já que dependeríamos da ordem que o bot verificar primeiro, portanto, o seguinte será feito:
                
                Maria será divorciada de Pedro por não ter saldo suficiente.
                Pedro também será divorciado de Rita (seu último casamento), já que ele só tinha dinheiro para manter 3.
                
                *Nesse cenário, se Maria fosse também, o último casamento de Pedro, apenas ela seria divorciada \
                e Pedro seguiria com 3 casamentos.*
                
                Sim, nesse cenário, Pedro perderá 2 casamentos, mesmo que ele conseguisse manter um deles. \
                Essa decisão é tomada para evitar ambiguidades e comportamentos inesperados.
                """,
                initialCost,
                userDailyCost,
                MarryCommand.MAX_GENERAL_MARRIAGES,
                MarryCommand.MAX_PRIVILEGED_MARRIAGES
        );
    }

    /**
     * This method is practically the same as {@link ofc.bot.domain.sqlite.repository.MarriageRepository#countByUserId(long) MarriageRepository.countByUserId(long)} except that
     * it excludes users that are not affected by the daily relationship taxes.
     * <p>
     * The value returned by this method can be used to check how much the
     * given user will be charged daily by doing {@code findAffectedCount(long) * Marry.DAILY_COST}.
     *
     * @param userId The id of the user to run the count on.
     * @return The amount of user's relationships affected by the daily tax.
     */
    public int findAffectedCount(long userId) {
        List<Long> exclIds =  exclRepo.findUserIdsByType(ExclusionType.MARRIAGE_FEE);
        return marrRepo.countWithExclusions(userId, exclIds);
    }
}