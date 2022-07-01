package rtree

sealed class Either<out A, out B> {

    abstract val isLeft: Boolean
    abstract val isRight: Boolean

    fun swap(): Either<B, A> = fold({ Right(it) }, { Left(it) })

    inline fun <C> fold(ifLeft: (A) -> C, ifRight: (B) -> C): C = when (this) {
        is Left -> ifLeft(this.value)
        is Right -> ifRight(this.value)
    }

    data class Left<out A> internal constructor(val value: A) : Either<A, Nothing>() {
        override val isLeft: Boolean = true
        override val isRight: Boolean = false
    }

    data class Right<out B> internal constructor(val value: B) : Either<Nothing, B>() {
        override val isLeft: Boolean = false
        override val isRight: Boolean = true
    }

    companion object {
        fun <A, B> left(value: A): Either<A, B> = Left(value)
        fun <A, B> right(value: B): Either<A, B> = Right(value)
    }
}

fun <A, B> A.eitherLeft(): Either<A, B> = Either.left(this)
fun <A, B> B.eitherRight(): Either<A, B> = Either.right(this)
