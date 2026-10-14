package io.github.kevinah95.l07_activity.core

interface GameSession {
    fun spawn(
        gameObject: GameObject,
        x: Double = gameObject.x,
        y: Double = gameObject.y
    )

    fun despawn(gameObject: GameObject)
}

open class GameObject {
    // Físicas y posición
    var x: Double = 0.0
    var y: Double = 0.0
    var vx: Double = 0.0
    var vy: Double = 0.0
    var mass: Double = 0.0
    var bounce: Double = 0.0
    var friction: Double = 0.0

    // Render y dimensiones
    var spriteName: String = ""
    var spriteWidth: Int = 16
    var spriteHeight: Int = 16

    // Lógica y colisiones
    var tags: Int = Tags.NONE
    var collisionMask: Int = Tags.NONE
    var hp: Int = 0
    var maxHp: Int = 0
    var despawnOnCollision: Boolean = false
    var despawnOnBounce: Boolean = false
    var groupId: Int = 0

    // Callbacks
    var onCollisionAction: ((GameObject) -> Unit)? = null
    var onBounceAction: (() -> Unit)? = null

    var gameSession: GameSession? = null

    fun isTagged(mask: Int): Boolean {
        return (tags and mask) != 0
    }

    /**
     * Determina si este objeto debe evaluar una colisión física con [target].
     *
     * El objeto debe ser diferente al objetivo y su máscara de colisión
     * debe coincidir con al menos una etiqueta del objetivo.
     */
    fun canCollideWith(target: GameObject): Boolean {
        return this !== target &&
                (collisionMask and target.tags) != 0
    }

    fun bounds(): Rect2D {
        return Rect2D(
            x,
            y,
            spriteWidth.toDouble(),
            spriteHeight.toDouble()
        )
    }

    fun center(): Point2D {
        return Point2D(
            x + spriteWidth / 2.0,
            y + spriteHeight / 2.0
        )
    }

    open fun onCollision(target: GameObject) {
        onCollisionAction?.invoke(target)

        if (despawnOnCollision) {
            gameSession?.despawn(this)
        }
    }

    open fun onBounce() {
        onBounceAction?.invoke()

        if (despawnOnBounce) {
            gameSession?.despawn(this)
        }
    }
}