package io.posa.feature.breeds.util

internal fun getRandomNoSwipeMessage(): String {
    return noSwipeMessages.random()
}

private val noSwipeMessages by lazy {
    listOf(
        "??? swipe LEFT?? on THIS baby?? 🤨",
        "nah we don’t do cat rejection here 🚫🐱",
        "that cat just saw what you did…",
        "swiping left is disabled. respect the floof.",
        "every cat is a 10. you’re the one being judged.",
        "the cat said no 💅",
        "you really tried that huh",
        "this app said: kindness only 😌",
        "caught in 4K attempting betrayal",
        "the council of cats has denied your request",
        "bro really said ‘nah’ to a cat 💀",
        "we don’t ghost cats here",
        "cat feelings > your preferences",
        "swipe left? in THIS economy?",
        "that was illegal. the cat filed a report.",
        "you thought we’d allow that?? 😭",
        "cat detected. respect required.",
        "denied. try loving it instead.",
        "this is a pro-cat environment only",
        "the cat blinked slowly at you… and you did THIS?",
        "emotional damage (to the cat)",
        "you swipe right or you swipe again",
        "not very purr-son of you",
        "the cat disapproves. heavily.",
        "you just lost 10 cat karma points",
        "that cat had dreams bro",
        "no bad vibes. only cat vibes.",
        "swiping left is not in the meow-nual",
        "cat said: ‘we’re done here’",
        "you had ONE job 😭",
        "imagine rejecting a cat. couldn’t be me.",
        "this action has been meow-nitored",
        "you’re on thin ice with the cats",
        "respect the whiskers pls",
        "this is why the cat distribution system is watching you",
        "we protect ALL floofs here",
        "you don’t choose the cat. the cat chooses you.",
        "left swipe? that’s a personality issue.",
        "cat logged this incident.",
        "be serious right now…"
    )
}