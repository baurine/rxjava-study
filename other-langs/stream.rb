# simple
r = [1,2,3,4,5,6,7,8,9].select {|i| i>5}.map {|i| i*10}.reduce(0) {|a,b| a+b}
puts "Final result: #{r}"

# log
r = [1,2,3,4,5,6,7,8,9]
    .select do |i|
      puts "Filter: #{i} > 5 --> #{i > 5}"
      i > 5
    end
    .map do |i|
      puts "Map: #{i} * 10 --> #{i * 10}"
      i * 10
    end
    .reduce(0) do |a,b|
      puts "Reduce: #{a} + #{b} --> #{a + b}"
      a + b
    end
puts "Final result: #{r}"
