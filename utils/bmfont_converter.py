import sys
from pathlib import Path
from typing import Optional, List
from xml.etree import ElementTree

INFO_ATTRS = ["face*", "size", "bold", "italic", "charset*", "unicode",
              "stretchH", "smooth", "aa", "padding", "spacing", "outline=0"]
COMMON_ATTRS = ["lineHeight", "base", "scaleW", "scaleH", "pages", "packed",
                "alphaChnl", "redChnl", "greenChnl", "blueChnl"]
PAGE_ATTRS = ["id", "file*"]
CHAR_ATTRS = ["id", "x", "y", "width", "height", "xoffset", "yoffset", "xadvance", "page", "chnl"]
KERNING_ATTRS = ["first", "second", "amount"]


def convert_to_libgdx_bmfont(file: Path, output_file: Optional[Path] = None):
    """
    Convert a XML .fnt file produced by https://github.com/soimy/msdf-bmfont-xml
    to a format readable by LibGDX.

    :param file: Input BMFont (.fnt) XML file.
    :param output_file: Optional output path, or None to use input.
    """
    print(f"Input file: {file}")

    if file.suffix != ".fnt":
        raise ValueError("File must be .fnt")
    xml = ElementTree.parse(file).getroot()
    output = ""

    # Info
    output += convert_bmfont_element("info", xml.find("info"), INFO_ATTRS)

    # Common
    output += '\n' + convert_bmfont_element("common", xml.find("common"), COMMON_ATTRS)

    # Pages
    for page in xml.find("pages"):
        output += '\n' + convert_bmfont_element("page", page, PAGE_ATTRS)

    # Chars
    chars_xml = xml.find("chars")
    output += f"\nchars count={chars_xml.attrib['count']}"
    for char in chars_xml:
        output += '\n' + convert_bmfont_element("char", char, CHAR_ATTRS)

    # Kernings, only non-zero ones.
    kernings_xml = [kn for kn in xml.find("kernings") if int(kn.attrib["amount"]) != 0]
    output += f"\nkernings count={len(kernings_xml)}"
    for kerning in kernings_xml:
        output += '\n' + convert_bmfont_element("kerning", kerning, KERNING_ATTRS)

    # Write to output file
    output_filename = output_file if output_file is not None else file
    file_handle = output_filename.open("w")
    file_handle.write(output)
    file_handle.close()

    print(f"Output file: {output_filename}")


def convert_bmfont_element(name: str, element: ElementTree.Element, attrs: List[str]) -> str:
    """
    Return a list of attributes in LibGDX's format from a XML element
    and a list of attributes to include. Values of attributes ending with * are quoted.

    :param name: The BMFont element name.
    :param element: XML element with attributes.
    :param attrs: Attributes of the XML to output in order.
    :return: The attributes string.
    """
    output = name + " "
    for attr in attrs:
        # Check if attribute has default value and extract it
        has_default_value = "=" in attr
        default_value = None
        if has_default_value:
            parts = attr.split("=")
            attr = parts[0]
            default_value = parts[1]

        # Quoted attributes end with a star
        quoted = "*" in attr
        if quoted:
            attr = attr[:-1]

        # Get value or use default
        use_default_value = has_default_value and attr not in element.attrib
        value = default_value if use_default_value else element.attrib[attr]
        value_str = f"\"{value}\"" if quoted else value

        # Append output
        output += f"{attr}={value_str} "

    return output[:-1]


# If an argument was passed, interpret it as the file to convert.
if len(sys.argv) >= 2:
    convert_to_libgdx_bmfont(Path(sys.argv[1]),
                             Path(sys.argv[2]) if len(sys.argv) >= 3 else None)
