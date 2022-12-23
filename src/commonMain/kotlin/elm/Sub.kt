package elm

/**
 *
 * The return type of the side-effecting [Update] containing:
 *
 *  -- in its [Pure] variant just the updated app.model
 *  -- in its [Effect] variant the updated app.model and a command to execute
 */
sealed class Sub<out Model, out Command> {
    abstract val model: Model
}

/**
 * You can think of the pure variant of Result as a type alias for pair(app.model,none)
 */
data class Pure<Model>(override val model: Model) : Sub<Model, Nothing>()

/**
 * You can think of the effect variant of Result as a type alias for pair(app.model,cmd)
 */
data class Effect<Model, Command>(override val model: Model, val cmd: Command) : Sub<Model, Command>()

interface ModelOperator

/**
 * Syntactic sugar for creating a [Pure], and we all now that too much sugar is bad for your health.
 *
 * Example:
 * app.model + none == elm.Pure(app.model)
 *
 */
operator fun <Model : ModelOperator> Model.plus(@Suppress("UNUSED_PARAMETER") none: None): Pure<Model> = Pure(this)

/* Marks that there are no commands.*/
object None

/**
 * Syntactic sugar for creating a [Effect], and we all now that too much sugar is bad for your health.
 *
 * Example:
 * app.model + app.app.ksn.update.Cmd.Increment == elm.Effect(app.model,app.app.ksn.update.Cmd.Increment)
 *
 */
operator fun <Model : ModelOperator, Command> Model.plus(that: Command): Sub<Model, Command> = Effect(this, that)
