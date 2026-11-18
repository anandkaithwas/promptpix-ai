# Aura Studio release builds use the Android default optimized R8 rules.
# Add narrowly scoped keep rules only when verified reflection/JNI contracts require them.
# Never add broad keep-all rules: they defeat obfuscation and code shrinking.
