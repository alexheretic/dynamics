Dynamics
------

Dynamics is a Java library for handling nested weakly-typed data

Initially developed to help handle JSON and XML messages in Java servers without tedious and repetitive null checking, type conversion & casting

### Weakly-Typed Nested Data Structure Handling

An `alexh.Dynamic` object is a weakly-typed, possible nested structure that allows null-safe child selection, creating the Dynamic wrapper is easy
```java
/* represents JSON:
    {
      "product": {
        "investment": {
          "investment-1": 12345.33,
          "investment-2": 43213.44
        },
        "id": "RR1209478",
        "effective": "2015-03-07T00:35:11"
      }
    }
 */
Map json = ...

Dynamic message = Dynamic.from(json);
```

Each 'get' call returns a non-null Dynamic instance that represents the child with that key, or the key's absence. Finally the wrapped object target can be accessed with `asObject()`, or `as(Class)` to cast.

```java
Dynamic investment1 = message.get("product").get("investment").get("investment-1");
if (investment1.isPresent())
    investment1.as(BigDecimal.class); // 12345.33
```

This allows a single call to `isPresent()` to indicate if the required value exists.

```java
message.get("product").get("one").get("two").get("three").get("four").isPresent(); // false

```

With String keys we can fetch deeply in a single get call by splitting the string into multiple gets and supplying a splitter string. In addition to `isPresent()` method a Dynamic my be unpacked into an Optional with `asOptional()` or can be wrapped into a Optional<Dynamic> with `maybe()`

```java
message.get("product.investment.investment-2", ".").maybe()
    .map(investment2 -> investment2.as(BigDecimal.class)); // Optional[43213.44]
```
#### Error Handling

Dynamic instances throw descriptive exceptions when data is missing, or not as selected.
```java
// exception message: 
// "'holdings' key is missing in path root->product->*holdings*->foo->bar, 
//   available root->product: Map[effective, investment, id]"
message.get("product.holdings.foo.bar", ".").asObject(); // throws
```
#### Type Conversion

Aiding permissive reading (desired in servers consuming external messages) Dynamics provides common usage type runtime conversions with the `Converter` class.
```java
Converter.convert("1234.4321").intoDecimal(); // BigDecimal 1234.4321
Converter.convert(1234.4321d).intoDecimal(); // BigDecimal 1234.4321
```
Usage is also built into Dynamic instances.
```java
message.get("product").get("effective").convert().intoLocalDateTime(); 
// LocalDateTime of 2015-03-07T00:35:12
```

### XML Dynamics
todo add readme entry...

<br/>

Dynamics is licensed under the [Apache 2.0 licence](http://www.apache.org/licenses/LICENSE-2.0.html).

### Releases

1.0 currently in development. Requiring JDK 1.8 or later.