package com.studiosrios.scoreboardpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaLoginGoogle(
    onLoginSuccess: () -> Unit,
    onEntrarComoTelespectador: () -> Unit // Novo callback
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo ScoreBoard Pro
        Image(
            painter = painterResource(id = R.drawable.logo_login),
            contentDescription = "Logo ScoreBoard Pro",
            modifier = Modifier.size(280.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "ScoreBoard Pro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
        
        Text(
            text = "Sua central esportiva profissional",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(60.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White
                ) {
                   Box(contentAlignment = Alignment.Center) {
                       Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                   }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Entrar com Google",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))

        // Botão para entrar como Telespectador
        OutlinedButton(
            onClick = onEntrarComoTelespectador,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
        ) {
            Text(
                text = "Entrar como Telespectador",
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "Ao entrar, você concorda com nossos Termos e Privacidade.",
            fontSize = 10.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
