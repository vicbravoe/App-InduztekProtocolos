package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.Validaciones
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.BotonSecundario
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.InduztekTheme

@Composable
fun LoginScreen(
    urlServidor: String,
    onGuardarUrl: (String) -> String?,
    onEntrar: (String, String, (String?) -> Unit) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var url by remember(urlServidor) { mutableStateOf(urlServidor) }
    var errorUrl by remember { mutableStateOf<String?>(null) }

    fun intentarEntrar() {
        errorCorreo = Validaciones.correo(correo)
        errorClave = Validaciones.clave(clave)
        errorGeneral = null
        if (errorCorreo == null && errorClave == null) {
            onEntrar(correo, clave) { mensaje -> errorGeneral = mensaje }
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Induztek Protocolos") },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text("Formalización de protocolos de prueba en terreno", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Ingresa con tu correo y contraseña. Cada rol ve y puede hacer cosas distintas.",
                style = MaterialTheme.typography.bodyMedium
            )
            TarjetaSeccion(titulo = "Iniciar sesión") {
                CampoTexto(
                    valor = correo,
                    onValorCambia = { correo = it; errorCorreo = null; errorGeneral = null },
                    etiqueta = "Correo electrónico",
                    icono = R.drawable.ic_correo,
                    error = errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = clave,
                    onValorCambia = { clave = it; errorClave = null; errorGeneral = null },
                    etiqueta = "Contraseña",
                    icono = R.drawable.ic_candado,
                    error = errorClave,
                    tipoTeclado = KeyboardType.Password,
                    esClave = true,
                    imeAction = ImeAction.Done,
                    onListo = { intentarEntrar() }
                )
                if (errorGeneral != null) {
                    Text(errorGeneral.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                BotonPrincipal(
                    texto = "Entrar",
                    icono = R.drawable.ic_persona,
                    onClick = { intentarEntrar() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TarjetaSeccion(titulo = "Servidor") {
                CampoTexto(url, { url = it; errorUrl = null }, "Dirección del API Gateway", R.drawable.ic_lista, error = errorUrl, tipoTeclado = KeyboardType.Uri)
                BotonSecundario(
                    texto = "Guardar y reconectar",
                    onClick = { errorUrl = onGuardarUrl(url) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    InduztekTheme {
        LoginScreen(
            urlServidor = "http://10.0.2.2:8080/",
            onGuardarUrl = { null },
            onEntrar = { _, _, _ -> }
        )
    }
}
