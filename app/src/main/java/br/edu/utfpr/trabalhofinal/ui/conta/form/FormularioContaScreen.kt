package br.edu.utfpr.trabalhofinal.ui.conta.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.trabalhofinal.R
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import br.edu.utfpr.trabalhofinal.ui.theme.TrabalhoFinalTheme
import br.edu.utfpr.trabalhofinal.ui.utils.composables.Carregando
import br.edu.utfpr.trabalhofinal.ui.utils.composables.ErroAoCarregar
import br.edu.utfpr.trabalhofinal.utils.toBrazilianDateFormat

@Composable
fun FormularioContaScreen(
    modifier: Modifier = Modifier,
    onVoltarPressed: () -> Unit,
    viewModel: FormularioContaViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    LaunchedEffect(viewModel.state.contaPersistidaOuRemovida) {
        if (viewModel.state.contaPersistidaOuRemovida) {
            onVoltarPressed()
        }
    }
    val context = LocalContext.current
    LaunchedEffect(snackbarHostState, viewModel.state.codigoMensagem) {
        viewModel.state.codigoMensagem.takeIf { it > 0 }?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onMensagemExibida()
        }
    }

    if (viewModel.state.mostrarDialogConfirmacao) {
        ConfirmationDialog(
            title = stringResource(R.string.atencao),
            text = stringResource(R.string.mensagem_confirmacao_remover_contato),
            onDismiss = viewModel::ocultarDialogConfirmacao,
            onConfirm = viewModel::removerConta
        )
    }

    val contentModifier: Modifier = modifier.fillMaxSize()
    if (viewModel.state.carregando) {
        Carregando(modifier = contentModifier)
    } else if (viewModel.state.erroAoCarregar) {
        ErroAoCarregar(
            modifier = contentModifier, onTryAgainPressed = viewModel::carregarConta
        )
    } else {
        Scaffold(modifier = modifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppBar(
                    contaNova = viewModel.state.contaNova,
                    processando = viewModel.state.salvando || viewModel.state.excluindo,
                    onVoltarPressed = onVoltarPressed,
                    onSalvarPressed = viewModel::salvarConta,
                    onExcluirPressed = viewModel::mostrarDialogConfirmacao
                )
            }) { paddingValues ->

            FormContent(
                modifier = modifier,

                processando = viewModel.state.salvando || viewModel.state.excluindo,
                descricao = viewModel.state.descricao,
                data = viewModel.state.data,
                valor = viewModel.state.valor,
                paga = viewModel.state.paga,
                tipo = viewModel.state.tipo,
                onDescricaoAlterada = viewModel::onDescricaoAlterada,
                onDataAlterada = viewModel::onDataAlterada,
                onValorAlterado = viewModel::onValorAlterado,
                onStatusPagamentoAlterado = viewModel::onStatusPagamentoAlterado,
                onTipoAlterado = viewModel::onTipoAlterado,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    confirmButtonText: String? = null
) {
    AlertDialog(modifier = modifier, title = title?.let {
        { Text(it) }
    }, text = { Text(text) }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = onConfirm
        ) {
            Text(confirmButtonText ?: stringResource(R.string.confirmar))
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text(dismissButtonText ?: stringResource(R.string.cancelar))
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    contaNova: Boolean,
    processando: Boolean,
    onVoltarPressed: () -> Unit,
    onSalvarPressed: () -> Unit,
    onExcluirPressed: () -> Unit
) {
    TopAppBar(modifier = modifier.fillMaxWidth(), title = {
        Text(
            if (contaNova) {
                stringResource(R.string.nova_conta)
            } else {
                stringResource(R.string.editar_conta)
            }
        )
    }, navigationIcon = {
        IconButton(onClick = onVoltarPressed) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.voltar)
            )
        }
    }, actions = {
        if (processando) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(60.dp)
                    .padding(all = 16.dp), strokeWidth = 2.dp
            )
        } else {
            if (!contaNova) {
                IconButton(onClick = onExcluirPressed) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.excluir)
                    )
                }
            }
            IconButton(onClick = onSalvarPressed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.salvar)
                )
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary
    )
    )
}

@Preview(showBackground = true)
@Composable
private fun AppBarPreview() {
    TrabalhoFinalTheme {
        AppBar(contaNova = true,
            processando = false,
            onVoltarPressed = {},
            onSalvarPressed = {},
            onExcluirPressed = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    modifier: Modifier = Modifier,
    processando: Boolean,
    descricao: CampoFormulario,
    data: CampoFormulario,
    valor: CampoFormulario,
    paga: CampoFormulario,
    tipo: CampoFormulario,
    onDescricaoAlterada: (String) -> Unit,
    onDataAlterada: (String) -> Unit,
    onValorAlterado: (String) -> Unit,
    onStatusPagamentoAlterado: (Boolean) -> Unit,
    onTipoAlterado: (TipoContaEnum) -> Unit,
    paddingValues: PaddingValues,
) {

    var showModalInput by remember { mutableStateOf(false) }
    var paidChecked by remember { mutableStateOf(paga.pago) }
    var radioSelected by remember { mutableStateOf(tipo.tipoConta) }



    Column(
        modifier = modifier

            .padding(paddingValues)
            .padding(all = 16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())

    ) {
        val formTextFieldModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Notes,
                contentDescription = stringResource(R.string.descricao),
                tint = MaterialTheme.colorScheme.outline
            )
            FormTextField(
                modifier = formTextFieldModifier,
                titulo = stringResource(R.string.descricao),
                campoFormulario = descricao,
                onValorAlterado = onDescricaoAlterada,
                keyboardCapitalization = KeyboardCapitalization.Words,
                enabled = !processando
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.AttachMoney,
                contentDescription = stringResource(R.string.valor),
                tint = MaterialTheme.colorScheme.outline
            )
            FormTextField(
                keyboardType = KeyboardType.Number,
                modifier = formTextFieldModifier,
                titulo = stringResource(R.string.valor),
                campoFormulario = valor,
                onValorAlterado = onValorAlterado,
                enabled = !processando
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val datePickerState = rememberDatePickerState()
            if (showModalInput) {
                DatePickerDialog(onDismissRequest = { showModalInput = false }, confirmButton = {
                    TextButton(onClick = {
                        showModalInput = false
                        datePickerState.selectedDateMillis?.let {
                            onDataAlterada(datePickerState.selectedDateMillis!!.toBrazilianDateFormat())
                        }

                    }) { Text("Ok") }
                }, dismissButton = {
                    TextButton(onClick = { showModalInput = false; }) { Text("Cancelar") }
                }) {
                    DatePicker(state = datePickerState)
                }
            }
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.data),
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            FormTextField(
                modifier = formTextFieldModifier,
                onClick = { showModalInput = true },
                titulo = stringResource(R.string.data),
                campoFormulario = data,
                onValorAlterado = onDataAlterada,
                keyboardType = KeyboardType.Number,
                keyboardCapitalization = KeyboardCapitalization.Words,
                enabled = false,
                readOnly = true,

                )
        }

        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(56.dp),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.paga),
                tint = MaterialTheme.colorScheme.outline
            )
            Checkbox(
                onCheckedChange = {
                    onStatusPagamentoAlterado(it)
                    paidChecked = it
                }, checked = paidChecked, enabled = !processando
            )
            Text("Pago")
        }

        Text(
            "Tipo de conta",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            TipoContaEnum.entries.forEach {
                RadioButton(
                    selected = radioSelected == it, onClick = {
                        radioSelected = it
                        onTipoAlterado(it)

                    }, enabled = !processando
                )
                Text(it.name)
            }

        }
    }
}

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    titulo: String,
    campoFormulario: CampoFormulario,
    onValorAlterado: (String) -> Unit,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardImeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
) {
    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick?.let { onClick() } },
            value = campoFormulario.valor,
            onValueChange = onValorAlterado,
            label = { Text(titulo) },
            maxLines = 1,
            enabled = enabled,
            isError = campoFormulario.contemErro,
            keyboardOptions = KeyboardOptions(
                capitalization = keyboardCapitalization,
                imeAction = keyboardImeAction,
                keyboardType = keyboardType
            ),
            visualTransformation = visualTransformation,
            readOnly = readOnly

        )
        if (campoFormulario.contemErro) {
            Text(
                text = stringResource(campoFormulario.codigoMensagemErro),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun FormContentPreview() {
    TrabalhoFinalTheme {
        FormContent(
            processando = false,
            descricao = CampoFormulario(),
            data = CampoFormulario(),
            valor = CampoFormulario(),
            paga = CampoFormulario(),
            tipo = CampoFormulario(),
            onDescricaoAlterada = {},
            onDataAlterada = {},
            onValorAlterado = {},
            onStatusPagamentoAlterado = {},
            onTipoAlterado = {},
            paddingValues = PaddingValues(16.dp)
        )
    }
}


@Preview
@Composable
private fun AllPreview() {
    TrabalhoFinalTheme {
        FormularioContaScreen(
            onVoltarPressed = {},
            viewModel = FormularioContaViewModel(
                savedStateHandle = SavedStateHandle()
            ),
            snackbarHostState = SnackbarHostState()
        )
    }

}