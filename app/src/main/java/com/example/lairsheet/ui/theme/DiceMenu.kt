package com.example.lairsheet.ui.theme

// Removed Image import; using Text inside the floating action button instead
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
// Import DiceIcon explicitly to avoid unresolved reference if file is not detected automatically
import com.example.lairsheet.ui.theme.DiceIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

/**
 * A composable that renders a floating dice roller menu.  The main button displays
 * an eight‑sided die and, when tapped, toggles visibility of additional dice
 * buttons.  Tapping any of the smaller dice invokes the supplied callback with
 * the number of sides for that die.  The menu sits anchored to the bottom–right
 * of its parent container.  Consumers are expected to overlay this composable
 * on top of their page content.
 *
 * @param expanded whether the menu of dice should be visible
 * @param onToggle called when the primary button is pressed to toggle menu state
 * @param onRoll invoked with the number of sides when a dice button is pressed
 * @param modifier optional modifier for positioning
 */
@Composable
fun DiceMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onRoll: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Container to position the menu relative to the bottom–right of the screen
    Box(modifier = modifier.fillMaxSize()) {
        // When expanded, show a small card with the various dice options.
        if (expanded) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // offset so it appears above the main FAB
                    .padding(end = 16.dp, bottom = 90.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display supported dice as text buttons.  If your project
                    // supplies dedicated SVG icons in the dice folder you can
                    // replace the Text with an Image using painterResource.
                    val diceList = listOf(4, 6, 8, 10, 12, 20)
                    diceList.forEach { sides ->
                        androidx.compose.material3.Button(
                            onClick = { onRoll(sides) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(4.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            // Draw a polygon representing the dice and overlay the number of sides.
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                DiceIcon(
                                    sides = sides,
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = sides.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary button representing an eight‑sided die.  Tapping this
        // toggles visibility of the menu defined above.
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            // Display the d8 icon in the primary floating button.  We draw an
            // eight‑sided polygon with a small numeral overlay to make
            // differentiation obvious.  If dedicated SVG assets are available,
            // you can replace this Box with an Image using painterResource.
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DiceIcon(
                    sides = 8,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                // Overlay a numeral inside the polygon to indicate the die.
                Text(
                    text = "8",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}