#!/bin/zsh
# made with help from ChatGPT (GPT-3.5) - An AI language model developed by OpenAI.

cd /Users/urbannovak/dev/GitHub/Java/MandelbrotSet

resultPath="./src/main/java/com/urbannovak/mandelbrotset/tests/results.csv"

# Define the test sizes (width and height)
sizes=(1000 2000 3000 4000 5000 6000 7000 8000 9000 10000 11000 12000 13000 14000 15000 16000 17000 18000 19000 20000)

# Define the computation modes
modes=(1 2 3) # For sequential, parallel, and distributed modes, respectively

# Create the results.csv file with headers
echo "Computation Mode,Size (px),Computation Time (ms)" > $resultPath

# Loop over sizes and modes
for size in "${sizes[@]}"; do
  for mode in "${modes[@]}"; do
    # Run the JavaFX application with specified arguments and append the output to the file
    for i in {0,1,2,3,5}; do
      echo "Running test: Width=$size Height=$size Mode=$mode";
      input_string=$(mvn javafx:run --quiet -Djavafx.args="$size $size $mode")
# Use 'awk' to extract the required information and remove "ms" and "px"
result=$(echo "$input_string" | awk -F ': ' '
  /Computation mode/ { mode = $2 }
  /Width/ { gsub("px", "", $2); width = $2 }
  /Computation time/ { gsub("ms", "", $2); time = $2 }
  END { print mode "," width "," time }
')
      echo "$result" >> $resultPath
    done
  done
done

echo "All tests completed. Results saved to results.csv"
