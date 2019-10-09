package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class MapDecoder : NonNullableDecoder<Map<*, *>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(Map::class.starProjectedType)

  override fun safeDecode(value: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Map<*, *>> {
    require(type.arguments.size == 2)

    val kType = type.arguments[0].type!!
    val vType = type.arguments[1].type!!

    fun <K, V> decode(node: MapValue,
                      kdecoder: Decoder<K>,
                      vdecoder: Decoder<V>,
                      registry: DecoderRegistry): ConfigResult<Map<*, *>> {

      return node.map.entries.map { (k, v) ->
        kdecoder.decode(StringValue(k, node.pos, node.dotpath), kType, registry).flatMap { kk ->
          vdecoder.decode(v, vType, registry).map { vv ->
            kk to vv
          }
        }
      }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toMap() }
    }

    return registry.decoder(kType).flatMap { kdecoder ->
      registry.decoder(vType).flatMap { vdecoder ->
        when (value) {
          is MapValue -> decode(value, kdecoder, vdecoder, registry)
          else -> ConfigFailure.UnsupportedCollectionType(value, "Map").invalid()
        }
      }
    }
  }
}
