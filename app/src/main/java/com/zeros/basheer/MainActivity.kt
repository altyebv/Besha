package com.zeros.basheer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.zeros.basheer.data.repository.DatabaseSeeder
import com.zeros.basheer.ui.components.BasheerBottomBar
import com.zeros.basheer.ui.navigation.BasheerNavHost
import com.zeros.basheer.ui.theme.BasheerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seeder: DatabaseSeeder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                seeder.seedFromAssets(this@MainActivity, "geography.json")
               seeder.seedQuizBankFromAssets(this@MainActivity)
                Log.d("Basheer", "Database seeded successfully!")
            } catch (e: Exception) {
                Log.e("Seeds", "Seeding failed", e)
            }
        }
        enableEdgeToEdge()
        setContent {
            BasheerTheme {
                val navController = rememberNavController()

                // Seed data on first launch
//                LaunchedEffect(Unit) {
//                    withContext(Dispatchers.IO) {
//                        seeder.seedInitialData()
//                    }
//                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BasheerBottomBar(navController = navController)
                    }
                ) { innerPadding ->
                    BasheerNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainActivityPreview() {
    BasheerTheme {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BasheerBottomBar(navController = navController)
            }
        ) { innerPadding ->
            BasheerNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
