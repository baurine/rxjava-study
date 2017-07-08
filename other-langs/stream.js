// simple
let r = [1,2,3,4,5,6,7,8,9].filter(i=>i>5).map(i=>i*10).reduce((a,b)=>a+b, 0)
console.log("Final result: " + r)

// log
r = [1,2,3,4,5,6,7,8,9]
    .filter(i => {
      console.log("Filter: " + i + " > 5 --> " + (i > 5))
      return i > 5
    })
    .map(i => {
      console.log("Map: " + i + " * 10 --> " + (i * 10))
      return i * 10
    })
    .reduce((a, b) => {
      console.log("Reduce: " + a + " + " + b + " --> " + (a + b))
      return a + b
    }, 0)
console.log("Final result: " + r)
