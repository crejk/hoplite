package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.net.InetAddress
import kotlin.reflect.KType

class InetAddressDecoder : BasicDecoder<InetAddress> {
  override fun supports(type: KType): Boolean = type.classifier == InetAddress::class
  override fun decode(node: Node, path: String): ConfigResult<InetAddress> = when (node) {
    is StringNode -> Try { InetAddress.getByName(node.value) }.toValidated {
      ThrowableFailure(it)
    }.toValidatedNel()
    else -> ConfigFailure.conversionFailure<Short>(node).invalidNel()
  }
}
