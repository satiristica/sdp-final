# sdp-final

* E-Commerce Shopping Cart - a console clothing store written in Java 21.
* Final project for the "Software Design Patterns" course, Astana IT University, SE-2539.

## Team

| Member | GitHub | Patterns |
|---|---|---|
| Alzhan Kainazarov | kc8, satiristica | Strategy, Facade |
| Daniyar Topaev | Daniyar-techit | Builder, Decorator |
| Azimbek Amandykov | Azimbek | Adapter, Observer |

## Features

- Browse the catalog loaded from an external supplier
- Customise a product (brand and material)
- Add gift wrapping
- Manage the cart and remove items
- Apply a discount
- Refresh supplier prices; the cart updates automatically
- Checkout with a simulated payment

## Design patterns

| Pattern | Classes | Purpose |
|---|---|---|
| Strategy | `DiscountStrategy`, `NoDiscount`, `PercentageDiscount` | Change the discount at runtime |
| Facade | `CheckoutFacade`, `PaymentService` | Checkout in one call |
| Builder | `ConfiguredProduct.Builder` | Create a product with a brand and material |
| Decorator | `ItemDecorator`, `GiftWrap` | Add gift wrapping to any cart item |
| Adapter | `SupplierCatalogAdapter`, `ExternalSupplier` | Convert supplier text rows into catalog products |
| Observer | `CatalogService`, `ProductObserver`, `ShoppingCart` | Update cart prices when the supplier changes them |

Errors are handled in one place by an AspectJ aspect (`ErrorHandlingAspect`) around `ConsoleStore.handle(String)`.

## UML diagram



## Tech stack

Java 21, Gradle (Kotlin DSL), AspectJ, JUnit 5, JaCoCo.

## Run

Run commands from the `ShoppingCart` folder.

```
cd ShoppingCart
./gradlew run --console=plain
./gradlew demo # checkout without the facade, for comparison
./gradlew test 
```

The coverage report is created after the tests in `app/build/reports/jacoco/test/html/index.html`.
