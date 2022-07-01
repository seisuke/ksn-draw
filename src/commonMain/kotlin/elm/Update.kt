package elm

import kotlinx.coroutines.flow.Flow

interface Update<Model, Message, Command> {

    fun update(msg: Message, model: Model): Sub<Model, Command>

    fun call(cmd: Command): Flow<Message>

    fun onUnhandledError(cmd: Command, t: Throwable): Message
}

interface PureUpdate<Model, Message> : Update<Model, Message, None> {

    override fun call(cmd: None): Flow<Message> {
        throw RuntimeException()
    }

    override fun onUnhandledError(cmd: None, t: Throwable): Message {
        throw RuntimeException()
    }
}
