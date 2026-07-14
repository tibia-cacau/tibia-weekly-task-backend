#!/bin/bash
# Fix line endings for Linux (convert CRLF to LF)

echo "Converting line endings to Unix format..."

# List of files to fix
FILES=(
    ".env"
    ".env.production"
    "start.sh"
    "stop.sh"
    "restart.sh"
    "test-env.sh"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  Fixing: $file"
        # Remove \r characters
        sed -i 's/\r$//' "$file"
    else
        echo "  Skipping: $file (not found)"
    fi
done

echo "✓ Line endings fixed!"
echo ""
echo "Making scripts executable..."
chmod +x start.sh stop.sh restart.sh test-env.sh 2>/dev/null
echo "✓ Done!"
