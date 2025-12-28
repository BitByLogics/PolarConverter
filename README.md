# PolarConverter

PolarConverter is a Java command-line tool for converting vanilla Minecraft worlds (Anvil format) into Polar world format, compatible with Minestom-based servers using the Polar engine. It supports full-world conversion or conversion of a specific chunk radius.

---

## Features

- Converts Minecraft worlds from Anvil to Polar format.
- Supports optional chunk radius conversion for partial worlds.
- Validates world version against the Minestom server version before converting.
- Minimal dependencies: Minestom, Polar.

---

## Requirements

- Java 25+
- Minecraft world in Java Edition Anvil format

> ⚠️ Only worlds matching the Minestom server’s supported DataVersion can be converted.

---

## Usage

```bash
java -jar PolarConverter.jar --world "path/to/world" --output "output_name" [--radius N]
```

### Arguments

| Argument | Description |
|----------|-------------|
| `--world "world folder"` | Path to the Minecraft world folder to convert. Must contain a `region` folder and `level.dat`. |
| `--output "world"` | Output file name for the converted Polar world (`.polar` extension will be added). |
| `--radius N` | Optional. Limits conversion to a radius of N chunks from 0,0. If omitted, the full world is converted. |

### Example

Convert an entire world:

```bash
java -jar PolarConverter.jar --world "MyMinecraftWorld" --output "MyPolarWorld"
```

Convert only a 10-chunk radius around spawn:

```bash
java -jar PolarConverter.jar --world "MyMinecraftWorld" --output "MyPolarWorld" --radius 10
```

---

## How It Works

1. **Minestom Initialization**  
   Starts a minimal offline Minestom server to provide block registries and NBT support.

2. **World Validation**  
   - Checks that `level.dat` exists.
   - Verifies `DataVersion` matches the Minestom server version.

3. **Conversion**
   - Uses `AnvilPolar` to read Anvil chunks.
   - Converts blocks and metadata to Polar format.
   - Writes the output to a `.polar` file using `PolarWriter`.

4. **Optional Chunk Radius**  
   Only processes chunks within the specified radius around `(0,0)`.

---

## Notes

- **DataVersion Mismatch:** If the world’s version does not match the Minestom server’s supported version, the converter will stop and display an error.

---

## Dependencies

- [Minestom](https://github.com/Minestom/Minestom) – Minecraft server library
- [Polar](https://github.com/HollowCube/Polar) – Polar world format support

---

## License

This project is licensed under the MIT License.
