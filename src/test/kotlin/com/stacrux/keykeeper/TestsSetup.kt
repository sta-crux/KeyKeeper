package com.stacrux.keykeeper

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Only for tests we expose the hidden layers in the Service Provider to have direct access for Unit Testing
 */
open class TestsSetup {

    private fun getSubServiceProvider(): Any {
        val serviceProviderClass = ServiceProvider::class
        val property = serviceProviderClass.declaredMemberProperties.first { it.name == "subServiceProvider" }
        property.isAccessible = true
        return property.getter.call()!!
    }

    val subServiceProvider = getSubServiceProvider() as ServiceProvider.SubServicesProvider

}