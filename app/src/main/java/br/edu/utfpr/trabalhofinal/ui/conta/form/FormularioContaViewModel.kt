package br.edu.utfpr.trabalhofinal.ui.conta.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import br.edu.utfpr.trabalhofinal.R
import br.edu.utfpr.trabalhofinal.data.ContaDatasource
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import br.edu.utfpr.trabalhofinal.ui.Arguments
import br.edu.utfpr.trabalhofinal.utils.formatar
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder

class FormularioContaViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idConta: Int = savedStateHandle
        .get<String>(Arguments.ID_CONTA)
        ?.toIntOrNull() ?: 0
    var state: FormularioContaState by mutableStateOf(FormularioContaState(idConta = idConta))
        private set

    init {
        if (state.idConta > 0) {
            carregarConta()
        }
    }

    fun carregarConta() {
        state = state.copy(
            carregando = true,
            erroAoCarregar = false
        )
        val conta = ContaDatasource.instance.findOne(state.idConta)
        state = if (conta == null) {
            state.copy(
                carregando = false,
                erroAoCarregar = true
            )
        } else {
            state.copy(
                carregando = false,
                conta = conta,
                descricao = state.descricao.copy(valor = conta.descricao),
                data = state.data.copy(valor = conta.data.formatar()),
                valor = state.valor.copy(valor = conta.valor.toString()),
                paga = state.paga.copy(pago = conta.paga),
                tipo = state.tipo.copy(tipoConta = conta.tipo)
            )
        }
    }

    fun onDescricaoAlterada(novaDescricao: String) {
        if (state.descricao.valor != novaDescricao) {
            state = state.copy(
                descricao = state.descricao.copy(
                    valor = novaDescricao,
                    codigoMensagemErro = validarDescricao(novaDescricao)
                )
            )
        }
    }

    private fun validarDescricao(descricao: String): Int = if (descricao.isBlank()) {
        R.string.descricao_obrigatoria
    } else {
        0
    }

    fun onDataAlterada(novaData: String) {
        if (state.data.valor != novaData) {
            state = state.copy(
                data = state.data.copy(
                    valor = novaData
                )
            )
        }
    }

    fun onValorAlterado(novoValor: String) {
        val filteredValue = novoValor.filter { it.isDigit() || it == '.' }

        if (state.valor.valor != filteredValue) {
            state = state.copy(
                valor = state.valor.copy(
                    valor = filteredValue,
                    codigoMensagemErro = validarValor(filteredValue)

                )
            )
        }
    }

    private fun validarValor(valor: String): Int = if (valor.isBlank()) {
        R.string.mandatory_value
    } else {
        0
    }

    fun onStatusPagamentoAlterado(novoStatusPagamento: Boolean) {
        if (state.paga.pago != novoStatusPagamento) {
            state = state.copy(
                paga = state.paga.copy(
                    pago = novoStatusPagamento
                )
            )
        }
    }

    fun onTipoAlterado(novoTipo: TipoContaEnum) {
        if (state.tipo.tipoConta != novoTipo) {
            state = state.copy(
                tipo = state.tipo.copy(
                    tipoConta = novoTipo
                )
            )
        }
    }

    private fun validateDate(): LocalDate {

        if (state.data.valor.isNotBlank()) {
            val formatter = DateTimeFormatterBuilder()
                .appendPattern("[dd/MM/yyyy][yyyy-MM-dd]")
                .toFormatter()
            return LocalDate.parse(state.data.valor, formatter)
        }
        return LocalDate.now();
    }


    fun salvarConta() {
        if (formularioValido()) {
            state = state.copy(
                salvando = true
            )
            val conta = state.conta.copy(
                descricao = state.descricao.valor,
                data = validateDate(),
                valor = BigDecimal(state.valor.valor),
                paga = state.paga.pago,
                tipo = state.tipo.tipoConta,
            )
            ContaDatasource.instance.salvar(conta)
            state = state.copy(
                salvando = false,
                contaPersistidaOuRemovida = true
            )
        }
    }

    private fun formularioValido(): Boolean {
        state = state.copy(
            descricao = state.descricao.copy(
                codigoMensagemErro = validarDescricao(state.descricao.valor)
            ),
            valor = state.valor.copy(
                codigoMensagemErro = validarValor(state.valor.valor)
            )
        )
        return state.formularioValido
    }

    fun mostrarDialogConfirmacao() {
        state = state.copy(mostrarDialogConfirmacao = true)
    }

    fun ocultarDialogConfirmacao() {
        state = state.copy(mostrarDialogConfirmacao = false)
    }

    fun removerConta() {
        state = state.copy(
            excluindo = true,
        )
        ContaDatasource.instance.remover(state.conta)
        state = state.copy(
            excluindo = false,
            contaPersistidaOuRemovida = true
        )
    }

    fun onMensagemExibida() {
        state = state.copy(codigoMensagem = 0)
    }
}