package me.elephant1214.unlimitedenchant.config.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Clamp(val min: Long = Long.MIN_VALUE, val max: Long = Long.MAX_VALUE)