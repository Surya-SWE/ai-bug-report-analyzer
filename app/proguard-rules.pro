# ProGuard rules for AI Bug Report Analyzer

# Keep everything in kotlinx.serialization and kotlinx.datetime
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.datetime.** { *; }
-keep class kotlinx.serialization.internal.** { *; }
-keep class kotlinx.serialization.descriptors.** { *; }
-keep class kotlinx.serialization.encoding.** { *; }
-keep class kotlinx.serialization.modules.** { *; }
-keep class kotlinx.serialization.json.** { *; }
-keep class kotlinx.serialization.protobuf.** { *; }
-keep class kotlinx.serialization.cbor.** { *; }
-keep class kotlinx.serialization.properties.** { *; }
-keep class kotlinx.serialization.KSerializer { *; }
-keep class kotlinx.serialization.Serializable { *; }
-keep class kotlinx.serialization.SerializationException { *; }
-keep class kotlinx.serialization.MissingFieldException { *; }
-keep class kotlinx.serialization.SealedClassSerializer { *; }
-keep class kotlinx.serialization.SerializationStrategy { *; }
-keep class kotlinx.serialization.DeserializationStrategy { *; }
-keep class kotlinx.serialization.UnknownFieldException { *; }
-keep class kotlinx.serialization.InternalSerializationApi { *; } 