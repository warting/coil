# SVGs

To add SVG support, import the extension library:

```kotlin
implementation("io.coil-kt.coil3:coil-svg:3.3.0")
```

And that's it! The `ImageLoader` will automatically detect and decode any SVGs. Coil detects SVGs by looking for the `<svg ` marker in the first 1 KB of the file, which should cover most cases. If the SVG is not automatically detected, you can set the `Decoder` explicitly for the request:

```kotlin
imageView.load("/path/to/svg") {
    decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
}
```

Optionally, you can manually add the decoder to your component registry when constructing your `ImageLoader`:

```kotlin
val imageLoader = ImageLoader.Builder(context)
    .components {
        add(SvgDecoder.Factory())
    }
    .build()
```
