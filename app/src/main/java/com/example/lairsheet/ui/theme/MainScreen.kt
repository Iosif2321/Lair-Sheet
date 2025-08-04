package com.example.lairsheet.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lairsheet.R
import com.example.lairsheet.data.Character
import com.example.lairsheet.data.Ruleset

@Composable
fun MainScreen(
    ruleset: Ruleset,
    characters: List<Character>,
    onRulesetChange: (Ruleset) -> Unit,
    onCreateCharacter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Header()

        Spacer(Modifier.height(16.dp))

        RulesToggle(
            selected = ruleset,
            onSelect = onRulesetChange
        )

        Spacer(Modifier.height(16.dp))

        CreateCharacterButton(onClick = onCreateCharacter)

        Spacer(Modifier.height(16.dp))

        CharacterGrid(
            items = characters
        )
    }
}

@Composable
private fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightPink)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_dragon_logo),
            contentDescription = "Dragon Logo",
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Lair Sheet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DeepRed
        )
    }
}

@Composable
private fun RulesToggle(
    selected: Ruleset,
    onSelect: (Ruleset) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SegmentedButton(
            text = "D&D5e 14",
            selected = selected == Ruleset.R5E_2014,
            onClick = { onSelect(Ruleset.R5E_2014) },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
        )
        SegmentedButton(
            text = "D&D5e 24",
            selected = selected == Ruleset.R5E_2024,
            onClick = { onSelect(Ruleset.R5E_2024) },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
        )
    }
}

@Composable
private fun SegmentedButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) CoralRed else LightPink
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else DeepRed
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CreateCharacterButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepRed,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "СОЗДАТЬ ПЕРСОНАЖА",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CharacterGrid(items: List<Character>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { c ->
            CharacterCard(item = c)
        }
    }
}

@Composable
private fun CharacterCard(item: Character) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LightPink
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dragon_logo),
                contentDescription = item.name,
                modifier = Modifier.size(72.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepRed
                )
                Text(
                    text = "${item.race} ${item.className} ${item.level}",
                    fontSize = 14.sp,
                    color = DeepRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    LairSheetTheme {
        MainScreen(
            ruleset = Ruleset.R5E_2014,
            characters = emptyList(),
            onRulesetChange = {},
            onCreateCharacter = {}
        )
    }
}
