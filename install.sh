#!/bin/sh
set -eu

# Configuration
BIN_DIR="$HOME/.local/bin"
SHARE_DIR="$HOME/.local/share/copysnap"

# Helper function to print usage
usage() {
    echo "Usage: $0 version"
    echo "  Installs the specified copysnap version from a GitHub release to '~/.local/bin' and '~/.local/share/copysnap'."
    echo "  version (required)"
    echo "    The version to install."
    echo "    Valid versions are those for which a release exists at https://github.com/johannesbuchholz/copysnap/releases"
    echo ""
    echo "Example Usage:"
    echo "  $0 1.2.3"
    exit 1
}

# Check arguments
if [ $# -ne 1 ]; then
    usage
fi

if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

VERSION="${1#v}"

WRAPPER_PATH="$BIN_DIR/copysnap"

mkdir -p "$BIN_DIR"
mkdir -p "$SHARE_DIR"

JAR_NAME="copysnap-$VERSION.jar"
DOWNLOAD_URL="https://github.com/johannesbuchholz/copysnap/releases/download/v$VERSION/$JAR_NAME"

TMP_FILE="$(mktemp)"

echo "Installing copysnap version $VERSION..."
echo "Downloading $JAR_NAME..."

wget --quiet --show-progress -O "$TMP_FILE" "$DOWNLOAD_URL" || {
    echo "Error: download failed"
    echo "URL: $DOWNLOAD_URL"
    echo "Does version $VERSION exists at https://github.com/johannesbuchholz/copysnap/releases?"
    rm -f "$TMP_FILE"
    exit 1
}

if [ ! -s "$TMP_FILE" ]; then
    echo "Error: downloaded file is empty"
    rm -f "$TMP_FILE"
    exit 1
fi

mv -f "$TMP_FILE" "$SHARE_DIR/$JAR_NAME"

cat > "$WRAPPER_PATH" <<EOF
#!/bin/sh
exec java -jar "$SHARE_DIR/$JAR_NAME" "\$@"
EOF

chmod +x "$WRAPPER_PATH"

echo "Installation complete!"
echo "copysnap installed at: $WRAPPER_PATH"

if ! echo "$PATH" | grep -q "$BIN_DIR"; then
    echo ""
    echo "WARNING: $BIN_DIR is not in your PATH"
    echo "Add this to your shell config:"
    echo "  export PATH=\"\$HOME/.local/bin:\$PATH\""
fi