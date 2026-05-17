package leo.rios.officium.verifyProfile.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import leo.rios.officium.R
import leo.rios.officium.core.navigation.VerifyCompanyProfile
import leo.rios.officium.core.navigation.VerifyData
import leo.rios.officium.core.navigation.VerifyUnemployedProfile

@Composable
fun VerifyProfileScreen(verifyData: VerifyData, navigateTo: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF7F7F7),
                        Color(0xFFEDEDED)
                    )
                )
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "English (US)",
            color = Color(0xFF46525E),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))


        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Officium",
            modifier = Modifier.size(400.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Tipo de perfil", fontSize = 24.sp)
//            Text(text = verifyData.emailApp, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { navigateTo.navigate(VerifyUnemployedProfile(verifyData)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Crear perfil de desempleado",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { navigateTo.navigate(VerifyCompanyProfile(verifyData)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(
                    text = "Crear perfil de empresa",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))
    }
}
