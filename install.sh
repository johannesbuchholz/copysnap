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

VERSION="${1#v}"

WRAPPER_PATH="$BIN_DIR/copysnap"

mkdir -p "$BIN_DIR"
mkdir -p "$SHARE_DIR"

JAR_NAME="copysnap-$VERSION.jar"
DOWNLOAD_URL="https://github.com/johannesbuchholz/copysnap/releases/download/v$VERSION/$JAR_NAME"

TMP_FILE="$(mktemp)"

echo "Installing copysnap version $VERSION..."
echo "Downloading $JAR_NAME..."

wget -O "$TMP_FILE" "$DOWNLOAD_URL" || {
    echo "Error: download failed"
    echo "URL: $DOWNLOAD_URL"
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