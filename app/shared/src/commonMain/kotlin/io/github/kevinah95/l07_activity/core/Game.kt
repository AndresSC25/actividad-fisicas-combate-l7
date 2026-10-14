package io.github.kevinah95.l07_activity.core

import io.github.kevinah95.l07_activity.entities.createSpell
import kotlin.math.abs

data class Stage(
    var width: Double = 400.0,
    var height: Double = 200.0,
    var floor: Double = 0.0,
    var ceiling: Double = 200.0
)

data class SpellState(
    var targetAngle: Double = 0.5,
    var basePower: Double = 380.0,
    var maxCasts: Int = 3,
    var casts: Int = 3,
    var castRechargeRate: Double = 1000.0,
    var castRechargeTimer: Double = 0.0
)

class Game : GameSession {
    val stage: Stage = Stage()
    val objects: MutableList<GameObject> = mutableListOf()
    lateinit var player: GameObject
    val spell: SpellState = SpellState()

    override fun spawn(gameObject: GameObject, x: Double, y: Double) {
        gameObject.x = x
        gameObject.y = y
        gameObject.gameSession = this
        objects.add(gameObject)
    }

    override fun despawn(gameObject: GameObject) {
        objects.remove(gameObject)
    }

    fun update(dtMs: Double) {
        updateSpells(dtMs)
        updatePhysics(dtMs)
    }

    private fun updateSpells(dtMs: Double) {
        if (spell.casts < spell.maxCasts) {
            spell.castRechargeTimer += dtMs
            if (spell.castRechargeTimer >= spell.castRechargeRate) {
                spell.casts += 1
                spell.castRechargeTimer = 0.0
            }
        }
    }

    /**
     * Bucle de simulación física en dos fases:
     * 1. Integración de movimiento, gravedad y rebotes en límites.
     * 2. Detección y respuesta a colisiones entre objetos.
     */
    fun updatePhysics(dtMs: Double) {
        val d = dtMs / 1000.0
        val currentObjects = objects.toList()

        // Fase 1: Movimiento, gravedad y rebotes
        for (obj in currentObjects) {
            // Integración de Euler: actualizar posición según velocidad
            obj.x += obj.vx * d
            obj.y += obj.vy * d

            // Aplicar gravedad a objetos con masa que estén en el aire
            if (obj.mass > 0.0 && obj.y > 0.0) {
                obj.vy -= obj.mass * d
            }

            val lower = stage.floor
            val upper = stage.ceiling - obj.spriteHeight

            // Restricción vertical contra suelo o techo
            if (obj.y < lower || obj.y > upper) {
                obj.y = clamp(obj.y, lower, upper)

                if (abs(obj.vy) >= 10.0 || obj.despawnOnBounce) {
                    obj.onBounce()
                }

                // Invertir la velocidad vertical aplicando el rebote
                obj.vy *= -obj.bounce
            }

            // Aplicar fricción cuando el objeto está sobre un límite vertical
            if (obj.y == lower || obj.y == upper) {
                obj.vx *= (1.0 - obj.friction)
            }

            // Eliminar hechizos que salgan de los límites horizontales
            if (obj.x < 0.0 || obj.x > stage.width) {
                if (obj.isTagged(Tags.SPELL)) {
                    despawn(obj)
                }
            }
        }

        // Fase 2: Colisiones entre objetos mediante AABB y bitmasks
        for (obj in currentObjects) {
            for (target in currentObjects) {
                if (
                    obj.canCollideWith(target) &&
                    obj.bounds().overlaps(target.bounds())
                ) {
                    obj.onCollision(target)
                }
            }
        }
    }

    fun damage(target: GameObject, amount: Int, dealer: GameObject? = null) {
        val dmg = Damage(amount, dealer)
        target.hp = clamp(target.hp - dmg.amount, 0, target.maxHp)
        if (target.hp <= 0) {
            die(target)
        }
    }

    fun die(target: GameObject) {
        despawn(target)
    }

    /**
     * TODO Paso 4.2: Lanza un hechizo proyectil si quedan cargas disponibles.
     * 1. Si spell.casts <= 0, retornar false.
     * 2. Decrementar spell.casts.
     * 3. Crear el proyectil con createSpell().
     * 4. Obtener el vector de dirección con vectorFromAngle(spell.targetAngle).
     * 5. Asignar vx = dir.x * spell.basePower y vy = dir.y * spell.basePower.
     * 6. Spawnear el proyectil en la posición del jugador.
     * 7. Retornar true.
     */
    fun castSpell(): Boolean {
        if (spell.casts <= 0) {
            return false
        }

        spell.casts -= 1

        val projectile = createSpell()
        val direction = vectorFromAngle(spell.targetAngle)

        projectile.vx = direction.x * spell.basePower
        projectile.vy = direction.y * spell.basePower

        val playerCenter = player.center()

        spawn(
            gameObject = projectile,
            x = playerCenter.x,
            y = playerCenter.y
        )

        return true
    }
}
