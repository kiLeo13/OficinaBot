package ofc.bot.util.content;

public enum MemberEmoji {
    MIKE(       "🥕",   "384056064569507840"),
    MAGOLA(     "🐖",   "329402038238576650"),
    CAROL(      "🍞",   "420744411484520458"),
    BIGO(       "🍉",   "974159685764649010"),
    ANJO(       "🍑",   "742729586659295283"),
    TITA(       "🐹",   "630147383241474059"),
    DEX(        "🥑",   "188858941332127745"),
    KANG(       "🍊",   "595988337635098664"),
    LEO13(      "🥭",   "596939790532739075"),
    FUARK(      "🥩",   "326159427566567434"),
    LINFF(      "🍌",   "577787431340736533"),
    MANTOVANI(  "🍁",   "110083224532701184"),
    SLIMINHO(   "🍏",   "406084669755359245"),
    LANAXUXU(   "🍣",   "605042229857353749"),
    NIKKI(      "🍅",   "607347725255704625"),
    POKHEN(     "🍭",   "617582248102985737"),
    PRIMO_GF(   "🧄",   "695450491500494869"),
    KALERA(     "🍊",   "276151129048350720"),
    LZ(         "🌙",   "529826385846468639"),
    MTS(        "🍈",   "311632884857569280"),
    ANELIO(     "🍨",   "695790719582863370"),
    ISA(        "🍹",   "879066149915099166"),
    LIPANGA(    "🌽",   "665982740277100558"),
    LIMAO(      "🍋",   "710212856158879784"),
    KAI(        "🍬",   "935332028382081055"),
    MEWRILLO(   "🍆",   "327632293189648394"),
    MYUU_ALT(   "🍒",   "727978798464630824"),
    PRIMO(      "🧄",   "192762951919337472"),
    ALEK(       "🍿",   "441731614725111818"),
    FILTER(     "🌿",   "183644042012131329"),
    LETICIA(    "🍩",   "308774564521836544"),
    MAYCON(     "✨",   "531638324386070569"),
    LOBA(       "🍓",   "485290494214012929"),
    MYUU_MAIN(  "🍒",   "183645448509194240"),
    SARINHA(    "🦋",   "538394563937566759"),
    DILHA(      "🐼",   "246044542577541120"),
    DBUENAS(    "🍵",   "739607439380578344"),
    NICOLE(     "🍥",   "511191684558618641"),
    PULICIAL(   "🍎",   "596142451199049728"),
    ANANAS(     "🍼",    "700011804147056702"),
    THOMAZ(     "⚡",   "183739962422722561");

    private final String emoji;
    private final String id;

    MemberEmoji(String emoji, String id) {
        this.emoji = emoji;
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static String emojiById(String userId) {
        MemberEmoji[] emojis = values();

        for (MemberEmoji me : emojis)
            if (me.id.equals(userId))
                return me.emoji;

        return null;
    }
}