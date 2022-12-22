package elm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

abstract class Element<Model, Message> private constructor() {

    protected abstract val stateFlow: StateFlow<Model>

    abstract fun accept(msg: Message)

    @Composable
    fun asState() = stateFlow.collectAsState()

    @Composable
    fun <T> mapAsState(
        transform: (Model) -> T
    ): State<T> {
        val initValue = transform(stateFlow.value)
        val flow = stateFlow.map { transform(it) }
        return flow.collectAsState(initValue)
    }

    @Composable
    fun <T> flowMapAsState(
        initValue: T,
        transformFlow: (StateFlow<Model>) -> Flow<T>
    ): State<T> {
        val flow = transformFlow(stateFlow)
        return flow.collectAsState(initValue)
    }

    companion object {

        fun <Model, Message, Command> create(
            initial: Model,
            update: Update<Model, Message, Command>,
            coroutineScope: CoroutineScope
        ): Element<Model, Message> {

            val mutableMessageFlow = MutableSharedFlow<Message>()
            val mutableStateFlow = MutableStateFlow(initial)

            mutableMessageFlow
                .scan(mutableStateFlow.value) { model, msg ->
                    val sub = update.update(msg, model)
                    if (sub is Effect) {
                        update.call(sub.cmd)
                            .onEach { subMsg ->
                                mutableMessageFlow.emit(subMsg)
                            }
                            .catch { t -> update.onUnhandledError(sub.cmd, t) }
                            .launchIn(CoroutineScope(Dispatchers.Default))
                    }
                    sub.model
                }
                .onEach { model -> mutableStateFlow.value = model }
                .launchIn(CoroutineScope(Dispatchers.Main))

            return object : Element<Model, Message>() {

                override val stateFlow = mutableStateFlow

                override fun accept(msg: Message) {
                    coroutineScope.launch {
                        mutableMessageFlow.emit(msg)
                    }
                }
            }
        }
    }
}
