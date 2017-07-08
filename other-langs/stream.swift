//: Playground - noun: a place where people can play

// simple
var r1 = [1,2,3,4,5,6,7,8,9].filter {$0>5}.map {$0*10}.reduce(0) {$0+$1}
print("Final result: \(r1)")

// log
var r2 = [1,2,3,4,5,6,7,8,9].filter {
        print("Filter: \($0) > 5 --> \($0 > 5)")
        return $0 > 5
    }.map { (i) -> Int in
        print("Map: \(i) * 10 --> \(i * 10)")
        return i * 10
    }.reduce(0) {
        print("Reduce: \($0) + \($1) --> \($0 + $1)")
        return $0 + $1
    }
print("Final result: \(r2)")
