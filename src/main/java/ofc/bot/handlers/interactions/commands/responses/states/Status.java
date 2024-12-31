package ofc.bot.handlers.interactions.commands.responses.states;

public enum Status implements InteractionResult {
    // These are separate constants to responses where only
    // embeds/images/videos are sent.
    OK(        State.OK),
    FAIL(      State.FAILURE),
    PROCESSING(State.PROCESSING, "> <a:loading:1293036166387601469> Processando..."),


    /* -------------------- PASSED -------------------- */
    DONE(                                       State.OK, "Pronto! 😎"),
    BALANCE_SET_SUCCESSFULLY(                   State.OK, "Saldo de %s definido para `$%s`."),
    TRANSACTION_SUCCESSFUL(                     State.OK, "Você transferiu `$%s` para %s!"),
    SUCCESSFULLY_DISCONNECTING_USERS(           State.OK, "Desconectando `%s` usuários de `%s`."),
    SUCCESSFULLY_MOVING_USERS(                  State.OK, "Movendo `%s` usuários para `%s`."),
    LOADING_CHANNEL_MESSAGES(                   State.OK, "> <a:loading:1293036166387601469> Baixando mensagens...\n> Você será avisado no privado ao concluir.\n> Mensagem mais antiga: `%s`.\n> Total atual: `%s`."),
    BIRTHDAY_ADDED_SUCCESSFULLY(                State.OK, "Aniversário de %s (`%s`) salvo com sucesso!"),
    BIRTHDAY_DELETED_SUCCESSFULLY(              State.OK, "Aniversário de <@%d> foi removido com sucesso."),
    DIVORCED_SUCCESSFULLY(                      State.OK, "É, parece que as coisas não deram certo por aqui e vocês tiveram que se separar. 😕"),
    CONFIRMATION_CODE_ALREADY_SENT(             State.OK, "Um código de confirmação já foi enviado em seu privado! Por favor, utilize-o para acessar este recurso."),
    MARRIAGE_PROPOSAL_SENT_SUCCESSFULLY(        State.OK, "Proposta enviada com sucesso."),
    ECONOMY_SUCCESSFULLY_UPDATED_BALANCE(       State.OK, "Saldo de %s atualizado: `$%s`."),
    DAILY_SUCCESSFULLY_COLLECT(                 State.OK, "> ✨ Você ganhou `$%s` em daily hoje!"),
    DAILY_SUCCESSFULLY_COLLECTED_BOOSTING(      State.OK, "> 💎 Hoje você ganhou `$%s` em daily, incluindo um adicional de `$%s` por ser Nitro Booster!"),
    WORK_SUCCESSFUL(                            State.OK, "> 💼 Parabéns pelo seu trabalho rs, você ganhou `$%s`!"),
    WORK_SUCCESSFUL_BOOSTING(                   State.OK, "> 👑 Eita 🥵 parabéns pelo seu trabalho, você ganhou `$%s` hoje, com um acréscimo de `$%s` por ser booster."),
    PROPOSAL_REMOVED_SUCCESSFULLY(              State.OK, "Proposta de casamento enviada à %s foi removida com sucesso."),
    ROLE_SUCCESSFULLY_ADDED_TO_MEMBER(          State.OK, "O cargo `%s` foi adicionado com sucesso à %s."),
    MARRIAGE_PROPOSAL_REJECTED_SUCCESSFULLY(    State.OK, "É, parece que não foi dessa vez 😔"),
    GROUP_OWNERSHIP_TRANSFERRED_SUCCESSFULLY(   State.OK, "A posse do grupo foi transferida com sucesso para %s!"),
    MARRIAGE_PROPOSAL_ACCEPTED_SUCCESSFULLY(    State.OK, "Pedido de casamento aceito com sucesso. \uD83D\uDE03"),
    USERINFO_RESET_SUCCESSFULLY(                State.OK, "Customizações do userinfo resetadas."),
    CONFIRMATION_CODE_SENT_SUCCESSFULLY_TIP(    State.OK, "Um código de confirmação foi enviado no seu privado! Utilize este código %s."),
    USERINFO_COLOR_SUCCESSFULLY_UPDATED(        State.OK, "Cor do userinfo atualizada com sucesso."),
    CREATING_GROUP(                             State.OK, "<a:loading:1293036166387601469> criando grupo..."),
    USERINFO_DESCRIPTION_SUCCESSFULLY_UPDATED(  State.OK, "Descrição do userinfo atualizada com sucesso."),
    USERINFO_FOOTER_SUCCESSFULLY_UPDATED(       State.OK, "Rodapé do userinfo ataulizado com sucesso."),
    ROLES_SUCCESSFULLY_BACKED_UP(               State.OK, "Foram devolvidos `%d` cargos com sucesso para `%s`."),
    POLL_CLOSED(                                State.OK, "Enquete `%s` foi fechada com sucesso."),
    POLL_REOPENED(                              State.OK, "Enquete `%s` foi reaberta com sucesso."),
    ALL_STAFF_LIST_MESSAGES_UPDATED(            State.OK, "Todas as mensagens encontradas foram editadas."),
    MESSAGES_SUCCESSFULLY_DELETED(              State.OK, "`%02d` mensagens foram apagadas com sucesso em `%s`."),
    GROUP_SUCCESSFULLY_DELETED(                 State.OK, "Seu grupo `%s` foi apagado com sucesso."),
    GROUP_SUCCESSFULLY_UPDATED(                 State.OK, "Os dados fornecidos foram aplicados com sucesso ao seu grupo."),
    GROUP_SUCCESSFULLY_CREATED(                 State.OK, "✅ Grupo %s criado com sucesso! Você pode usar o comando `/group member invite` até 4 (quatro) vezes de graça."),
    GROUP_CHANNEL_SUCCESSFULLY_CREATED(         State.OK, "O canal para seu grupo foi criado com sucesso em %s!"),
    GROUP_CHANNEL_SUCCESSFULLY_DELETED(         State.OK, "Canal de grupo de tipo `%s` foi deletado com sucesso."),
    MEMBER_SUCCESSFULLY_ADDED_TO_GROUP(         State.OK, "O membro %s foi adicionado com sucesso no seu grupo."),
    MEMBER_SUCCESSFULLY_REMOVED_FROM_GROUP(     State.OK, "O membro <@%d> foi removido com sucesso do seu grupo."),
    NOTHING_CHANGED_WITH_REASON(                State.OK, "Nada foi alterado, pois %s."),
    LEADERBOARD_IS_EMPTY(                       State.OK, "O placar de líderes está vazio."),
    MARRIAGE_LIST_IS_EMPTY(                     State.OK, "Nenhum casamento encontrado."),
    CONFIRM_GROUP_CHANNEL_CREATION(             State.OK, "> Tem certeza que deseja comprar este canal? Tipo: `%s`"),
    CONFIRM_GROUP_UPDATE(                       State.OK, "Você está prestes a alterar alguns atributos de nome e/ou cor seu grupo. Deseja confirmar?"),
    CONFIRM_GROUP_MEMBER_ADD(                   State.OK, "Você está prestes a adicionar %s ao seu grupo. Por favor, verifique o valor abaixo e confirme o pagamento."),
    CONFIRM_GROUP_MEMBER_REMOVE(                State.OK, "Você está prestes a remover %s de seu grupo. Por favor, confirme sua decisão."),
    MARRIAGE_PROPOSAL_LIST_IS_EMPTY(            State.OK, "Nenhuma proposta encontrada para os argumentos fornecidos."),
    SUCCESSFULLY_REMOVED_FROM_GROUP(            State.OK, "Você foi removido(a) do grupo `%s` com sucesso!"),
    ROLE_HAS_NO_MEMBERS(                        State.OK, "Não tem nenhum membro no cargo fornecido"),
    GUILD_HAS_NO_ICON(                          State.OK, "O servidor atual não tem nenhum ícone."),
    MEMBER_ALREADY_HAS_ROLE(                    State.OK, "O membro já tem o cargo `%s`."),
    NO_GIF_WAS_FOUND(                           State.OK, "Nenhum GIF encontrado."),
    NO_GUILD_AVATAR_PRESENT(                    State.OK, "O membro não possui um avatar específico para este servidor."),
    USER_DID_NOT_VOTE_IN_POLL(                  State.OK, "O usuário não votou nessa enquete."),


    /* -------------------- ERROR -------------------- */
    WAIT_BEFORE_WORK_AGAIN(                     State.FAILURE),
    PLEASE_WAIT_COOLDOWN(                       State.FAILURE, "Por favor, aguarde `%s`."),
    REQUEST_REJECTED(                           State.FAILURE, "Pedido negado."),
    NOT_IMPLEMENTED(                            State.FAILURE, "Comando não implementado."),
    CANNOT_DIVORCE_YOURSELF(                    State.FAILURE, "Você não pode divorciar-se de você mesmo."),
    USER_IS_NOT_MARRIED_TO_TARGET(              State.FAILURE, "Você não está casado(a) com %s"),
    MEMBER_NOT_FOUND(                           State.FAILURE, "Membro não encontrado."),
    CANNOT_LEAVE_YOUR_OWN_GROUP(                State.FAILURE, "Você não pode sair do seu próprio grupo."),
    USER_NOT_FOUND(                             State.FAILURE, "Usuário não encontrado."),
    COMMAND_IS_ALREADY_OPERATING(               State.FAILURE, "Este comando já está em execução! Por favor, aguarde!"),
    GROUP_NAMES_CANNOT_CONTAIN_EMOJIS(          State.FAILURE, "Nomes de grupos não podem ter emojis."),
    PAGE_DOES_NOT_EXIST(                        State.FAILURE, "A página fornecida não existe! Max: `%d`."),
    MEMBER_NOT_IN_GUILD(                        State.FAILURE, "O membro fornecido não está no servidor."),
    INVALID_COLOR_PROVIDED(                     State.FAILURE, "A cor fornecida é inválida."),
    DAILY_ALREADY_COLLECTED(                    State.FAILURE, "Você já pegou o daily hoje! Aguarde meia noite para usar novamente."),
    YOU_DO_NOT_OWN_A_GROUP(                     State.FAILURE, "Você não possui nenhum grupo."),
    CANNOT_TRANSFER_TO_YOURSELF(                State.FAILURE, "Você não pode transferir dinheiro para você mesmo."),
    CANNOT_TRANSFER_TO_BOTS(                    State.FAILURE, "Você não pode transferir dinheiro para outros bots."),
    STAFF_MESSAGES_UPDATE_STILL_IN_PROGRESS(    State.FAILURE, "Algumas mensagens de `staff-oficina` ainda estão sendo atualizadas! Por favor, aguarde."),
    EMOJI_OPTION_CAN_ONLY_CONTAIN_EMOJI(        State.FAILURE, "A opção `emoji` pode apenas conter um emoji! Se você forneceu um emoji válido mas ele não foi reconhecido pelo bot, entre em contado com <@596939790532739075>."),
    INVALID_CHANNEL_TYPE(                       State.FAILURE, "O tipo de canal fornecido é inválido: `%s`."),
    FAILED_TO_DM_USER(                          State.FAILURE, "Não foi possível enviar mensagem em seu privado! Por favor, abra-o para que você possa prosseguir com o utilizo deste comando/recurso."),
    USER_CANNOT_RECEIVE_GIVEN_AMOUNT(           State.FAILURE, "Este usuário não pode receber esta quantia! Possivelmente ultrapassaria o limite de saldo."),
    NO_NAME_HISTORY_FOR_USER(                   State.FAILURE, "Sem histórico de nomes para `%s`."),
    YOU_ARE_NOT_IN_THE_PROVIDED_GROUP(          State.FAILURE, "Você não está no grupo fornecido."),
    MEMBER_ALREADY_IN_THE_GROUP(                State.FAILURE, "O membro fornecido já está no grupo."),
    MEMBER_NOT_IN_THE_GROUP(                    State.FAILURE, "O membro fornecido não está no grupo."),
    SAME_CHANNEL_FOR_MULTIPLE_ARGUMENTS(        State.FAILURE, "Você não pode fornecer o mesmo canal de voz em ambos os argumentos."),
    VOICE_CHANNEL_IS_EMPTY(                     State.FAILURE, "O canal de voz `%s` está vazio."),
    NO_USERS_DISCONNECTED(                      State.FAILURE, "Nenhum usuário foi desconectado."),
    NO_USERS_MOVED(                             State.FAILURE, "Nenhum usuário foi movido."),
    GROUPS_CANNOT_BE_CREATED_AT_THE_MOMENT(     State.FAILURE, "Grupos não estão sendo criados no momento! Para mais informações, entre em contato com algum staff."),
    GROUP_ALREADY_HAS_THE_PROVIDED_CHANNEL(     State.FAILURE, "O grupo já possui o tipo de canal fornecido."),
    NAME_TOO_SHORT(                             State.FAILURE, "Nomes precisam ter %d ou mais caracteres."),
    FAILED_TO_VALIDATE_PASSPHRASE(              State.FAILURE, "Não foi possível validar a frase de confirmação."),
    INCORRECT_CONFIRMATION_VALUE(               State.FAILURE, "O valor de confirmação está incorreto."),
    INVALID_DATE_FORMAT(                        State.FAILURE, "A data fornecida não segue o padrão esperado ou se refere à um momento inexistente! Por favor, utilize o padrão informado."),
    COULD_NOT_ADD_BIRTHDAY(                     State.FAILURE, "Não foi possível salvar aniversário! Verifique console para mais informações sobre o erro."),
    USER_IS_NOT_IN_BIRTHDAY_LIST(               State.FAILURE, "O usuário não está na lista de aniversariantes."),
    GROUP_NOT_FOUND(                            State.FAILURE, "Grupo não encontrado."),
    PASSPHRASES_MISMATCH(                       State.FAILURE, "Frase confirmatória está incorreta."),
    CHANNEL_MUST_HAVE_ONE_NATURAL_MESSAGE(      State.FAILURE, "O canal precisa ter pelo menos uma mensagem normal."),
    CONFIRMATION_CODE_MISMATCH(                 State.FAILURE, "Código incorreto."),
    GROUP_ROLE_NOT_FOUND(                       State.FAILURE, "Cargo para o grupo `%s` não foi encontrado."),
    COULD_NOT_REMOVE_BIRTHDAY(                  State.FAILURE, "Não foi possível remover o aniversário do usuário fornecido."),
    NO_RESULT_FOUND(                            State.FAILURE, "Nenhum resultado encontrado."),
    INVALID_VALUE_PROVIDED(                     State.FAILURE, "Valor inválido fornecido: `%s`."),
    GROUP_CHANNELS_CANNOT_BE_CREATED(           State.FAILURE, "Canais para grupos não estão sendo criados no momento."),
    VALUE_IS_LESS_OR_EQUAL_TO_ZERO(             State.FAILURE, "O valor fornecido não pode ser menor ou igual a zero."),
    CANNOT_MARRY_TO_USER(                       State.FAILURE, "Você não pode se casar com você mesmo ou com outros bots."),
    PENDING_PROPOSAL(                           State.FAILURE, "Você ou esta pessoa já têm uma proposta de casamento enviada para o outro! Por favor, aguarde que seja aceito/recusado. Ou se foi você que enviou, você pode cancelar com `/marriage cancel`."),
    ALREADY_MARRIED_TO_USER(                    State.FAILURE, "Você já está casado(a) com %s!"),
    USERS_CANNOT_HAVE_MULTIPLE_GROUPS(          State.FAILURE, "Cada usuário pode ter até, no máximo, 1 grupo."),
    ISSUER_HIT_MARRIAGE_LIMIT(                  State.FAILURE, "Você já chegou no limite de casamentos."),
    TARGET_HIT_MARRIAGE_LIMIT(                  State.FAILURE, "O membro que você quer se casar já chegou no limite de casamentos."),
    MARRIAGE_INSUFFICIENT_BALANCE(              State.FAILURE, "Saldo insuficiente! Ambos precisam ter `$%s` inicialmente para se casar."),
    CHANNEL_NOT_FOUND(                          State.FAILURE, "Canal não encontrado."),
    COULD_NOT_CONVERT_DATA_FROM_FILE(           State.FAILURE, "Não foi possível executar conversão dos dados! Verifique o formato do arquivo `%s` e tente novamente."),
    COMMAND_IS_ALREADY_RUNNING(                 State.FAILURE, "Este comando já está em execução! Por favor, aguarde."),
    CHANNEL_CATEGORY_NOT_FOUND(                 State.FAILURE, "A categoria do canal não foi encontrada."),
    CANNOT_UPDATE_SELF_MARRIAGE_DATE(           State.FAILURE, "Você não pode alterar a data de um casamento de você com você mesmo."),
    PROVIDED_USERS_ARE_NOT_MARRIED(             State.FAILURE, "Os usuários fornecidos não estão casados."),
    NO_ACTIVE_PROPOSAL_SENT_TO_USER(            State.FAILURE, "Você não tem nenhuma proposta de casamento enviada à %s."),
    NO_INCOME_PROPOSAL_FROM_USER(               State.FAILURE, "%s não te mandou nenhum pedido de casamento."),
    CANNOT_ACCEPT_SELF_MARRIAGE_PROPOSAL(       State.FAILURE, "Você não pode aceitar uma proposta de casamento enviada por você mesmo."),
    INVALID_HEX_PROVIDED(                       State.FAILURE, "Cores em HEX precisam conter exatamente `6` dígitos, opcionalmente acompanhados de um `#` (hashtag) no início.\nUse de auxílio: [__Google — Colour Picker__](<https://g.co/kgs/YKFnVZZ>)."),
    ROLE_NOT_FOUND(                             State.FAILURE, "Cargo não encontrado."),
    ROLES_NOT_FOUND_TO_BACKUP(                  State.FAILURE, "Nenhum cargo foi encontrado no backup."),
    ROLE_NOT_FOUND_BY_ID(                       State.FAILURE, "Nenhum cargo encontrado para o id `%s`."),
    INSUFFICIENT_BALANCE_VALUE(                 State.FAILURE, "> ❌ Você não tem saldo suficiente para esta operação!\nFalta: `$%s`."),
    INSUFFICIENT_BALANCE(                       State.FAILURE, "> ❌ Saldo insuficiente."),
    IP_NOT_FOUND(                               State.FAILURE, "IP não encontrado."),
    ISSUER_NOT_IN_VOICE_CHANNEL(                State.FAILURE, "Você não está conectado em nenhum canal de voz."),
    TARGET_MAY_NOT_BE_A_BOT(                    State.FAILURE, "O usuário fornecido não pode ser um bot."),
    INVALID_IP_ADDRESS_FORMAT(                  State.FAILURE, "O IP `%s` é inválido."),
    INCORRECT_CHANNEL_OF_USAGE(                 State.FAILURE, "Canal incorreto."),
    POLL_NOT_FOUND(                             State.FAILURE, "Enquete não encontrada."),
    FILE_TOO_LARGE(                             State.FAILURE, "O arquivo é grande demais, máximo: 25MB."),
    POLL_ALREADY_CLOSED_BY(                     State.FAILURE, "Essa enquete já foi fechada por <@%d> em <t:%d>."),
    POLL_IS_NOT_CLOSED(                         State.FAILURE, "Essa enquete não está fechada."),
    BASE_GROUP_ROLE_NOT_FOUND(                  State.FAILURE, "Cargo base necessário não foi encontrado."),
    OWNER_MEMBER_NOT_FOUND(                     State.FAILURE, "O dono fornecido não foi encontrado no servidor."),
    NO_GROUP_CHANNEL_FOUND(                     State.FAILURE, "Nenhum canal de grupo encontrado."),

    COULD_NOT_EXECUTE_SUCH_OPERATION(           State.FAILURE, "Não foi possível completar a operação.");

    private final State state;
    private final String data;

    Status(State state, String data) {
        this.state = state;
        this.data = data;
    }

    Status(State state) {
        this(state, null);
    }

    public InteractionResult args(Object... args) {
        return new ResultData(this, args);
    }

    @Override
    public InteractionResult setEphm(boolean ephm) {
        return new ResultData(this, ResultData.EMPTY_ARGS, ephm);
    }

    @Override
    public Status getStatus() {
        return this;
    }

    @Override
    public String getContent() {
        return this.data;
    }

    @Override
    public boolean isEphemeral() {
        return this.state == State.FAILURE;
    }

    @Override
    public boolean isOk() {
        return this.state == State.OK;
    }

    enum State {
        OK,
        PROCESSING,
        FAILURE
    }
}