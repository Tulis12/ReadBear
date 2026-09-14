package dev.tulis.readbear.routes.menu.quotes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuotesLibrary() {
    val quotes = arrayOf(
        "The hardest battles are often fought in silence.",
        "What we choose to remember shapes who we become.",
        "Time does not erase everything; sometimes it only teaches us how to carry it.",
        "A quiet mind can hear what a restless one cannot.",
        "Not every loss is meant to be recovered from.",
        "The truth does not become less true because it is difficult to accept.",
        "Some roads are worth walking even when they lead nowhere.",
        "We often understand the value of a moment only after it has become a memory.",
        "There are things that cannot be changed, only understood.",
        "The absence of certainty is not the absence of meaning.",
        "Growing older is learning which questions deserve an answer.",
        "A person can survive many things, but not without being changed by them.",
        "Sometimes moving forward means accepting that you cannot take everything with you.",
        "The past explains us, but it does not have to define us.",
        "Silence can be an answer when words would only make things smaller.",
        "You cannot hold on to everything and still have your hands free for what comes next.",
        "Some truths arrive quietly, long after we stop looking for them.",
        "What matters most is often invisible until it is gone.",
        "We become different people in the places where we once thought we would remain the same.",
        "Even the things we leave behind can continue to shape the path ahead."
    )

    val state = rememberLazyListState()

    LazyColumn {
        items(quotes.count()) { quoteIndex ->
            val quote = quotes[quoteIndex]

            Card(
                modifier = Modifier.padding(5.dp).fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Text(quote, modifier = Modifier.padding(15.dp))
            }
        }
    }
}