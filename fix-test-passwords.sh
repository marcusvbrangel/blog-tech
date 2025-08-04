#!/bin/bash

# Script to fix weak passwords in test files
# Replaces common weak passwords with strong test passwords

# Find all Java test files and replace weak passwords
find src/test/java -name "*.java" -type f -exec sed -i 's/"password123"/"TestPass123!"/g' {} \;
find src/test/java -name "*.java" -type f -exec sed -i 's/"password"/"TestPass123!"/g' {} \;
find src/test/java -name "*.java" -type f -exec sed -i 's/"newpassword123"/"NewPass456@"/g' {} \;
find src/test/java -name "*.java" -type f -exec sed -i 's/"testpassword"/"TestPass123!"/g' {} \;
find src/test/java -name "*.java" -type f -exec sed -i 's/"admin123"/"AdminPass789#"/g' {} \;

echo "Password fixes applied to test files"

# Show changed files
echo "Files modified:"
find src/test/java -name "*.java" -type f -exec grep -l "TestPass123!\|NewPass456@\|AdminPass789#" {} \;