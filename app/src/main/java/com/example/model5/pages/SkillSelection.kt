package com.example.model5.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.model5.MenteeViewModel

@Composable
fun SkillSelectionPage(navController: NavController, menteeViewModel: MenteeViewModel) {
    val predefinedSkills = listOf("Java", "Kotlin", "Android", "UI/UX Design","Python", "Machine Learning", "Data Analysis")


    val selectedSkills = remember { mutableStateListOf<String>() }


    val menteeDetails by menteeViewModel.menteeDetails.collectAsState()


    LaunchedEffect(menteeDetails) {
        selectedSkills.clear()
        selectedSkills.addAll(menteeDetails.selectedSkills)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Select Your Skills",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        predefinedSkills.forEach { skill ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        if (selectedSkills.contains(skill)) {
                            selectedSkills.remove(skill)
                        } else {
                            selectedSkills.add(skill)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedSkills.contains(skill),
                    onCheckedChange = {
                        if (selectedSkills.contains(skill)) {
                            selectedSkills.remove(skill)
                        } else {
                            selectedSkills.add(skill)
                        }
                    }
                )
                Text(text = skill, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                menteeViewModel.updateMenteeSkills(selectedSkills.toList())
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
        ) {
            Text(text = "Save Skills")
        }
    }
}
