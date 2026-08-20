import model.*
import model.enum.*
import repository.*
import java.math.BigDecimal
import java.time.LocalDate

object SampleDataGenerator {

    fun load() {
        // ── Admin ──────────────────────────────────────────────────────────────
        val admin = Admin(name = "Priya Raman", dob = LocalDate.of(1990, 4, 12), gender = Gender.FEMALE)
        AdminRepository.add(admin)

        // ── Food products ──────────────────────────────────────────────────────
        val lays = Food(
            productName = "Lay's Classic Salted", brand = "Lay's",
            description = "Crispy potato chips, classic salted",
            price = BigDecimal("20"),
            vegOrNonVeg = VegNonVeg.VEG,
            ingredients = mutableListOf("Potato", "Vegetable Oil", "Salt"),
            shelfLifeMonths = 5, foodType = FoodType.CHIP
        )
        val bisleri = Food(
            productName = "Bisleri Mineral Water", brand = "Bisleri",
            description = "Packaged drinking water 500ml",
            price = BigDecimal("15"),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Purified Water"),
            shelfLifeMonths = 1, foodType = FoodType.WATER_BOTTLE
        )
        val cocaCola = Food(
            productName = "Coca-Cola", brand = "Coca-Cola",
            description = "Carbonated soft drink 300ml",
            price = BigDecimal("40"),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Carbonated Water", "Sugar", "Caramel Color", "Caffeine"),
            shelfLifeMonths = 8, foodType = FoodType.COLD_DRINK, warning = "Contains caffeine"
        )
        val dairyMilk = Food(
            productName = "Cadbury Dairy Milk", brand = "Cadbury",
            description = "Milk chocolate bar 40g",
            price = BigDecimal("50"),
            vegOrNonVeg = VegNonVeg.VEG,
            ingredients = mutableListOf("Milk Solids", "Sugar", "Cocoa Butter", "Cocoa Solids"),
            shelfLifeMonths = 9, foodType = FoodType.CHOCOLATE_BAR, warning = "Contains milk"
        )
        val monster = Food(
            productName = "Monster Energy", brand = "Monster",
            description = "Energy drink 350ml",
            price = BigDecimal("110"),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Carbonated Water", "Sugar", "Taurine", "Caffeine", "B-Vitamins"),
            shelfLifeMonths = 6, foodType = FoodType.ENERGY_DRINK,
            warning = "High caffeine — not for children"
        )
        listOf(lays, bisleri, cocaCola, dairyMilk, monster).forEach {
            FoodRepository.add(it)
            ProductRepository.add(it)
        }

        // ── ProductionBatches ──────────────────────────────────────────────────
        // Slot 1: Lays — two batches from Coimbatore (FIFO: older sold first)
        val laysBatch1 = ProductionBatch(
            productId = lays.productId,
            manufacturingLocation = Location.COIMBATORE,
            manufacturingDate = LocalDate.now().minusMonths(1),
            expiryDate = lays.calculateExpiryDate(LocalDate.now().minusMonths(1)),
            quantity = 10
        )
        val laysBatch2 = ProductionBatch(
            productId = lays.productId,
            manufacturingLocation = Location.COIMBATORE,
            manufacturingDate = LocalDate.now().minusDays(5),
            expiryDate = lays.calculateExpiryDate(LocalDate.now().minusDays(5)),
            quantity = 5
        )

        // Slot 2: Bisleri + CocaCola
        val bisleriBatch = ProductionBatch(
            productId = bisleri.productId,
            manufacturingLocation = Location.PORUR,
            manufacturingDate = LocalDate.now().minusDays(10),
            expiryDate = bisleri.calculateExpiryDate(LocalDate.now().minusDays(10)),
            quantity = 20
        )
        val colaOldBatch = ProductionBatch(
            productId = cocaCola.productId,
            manufacturingLocation = Location.TAMBARAM,
            manufacturingDate = LocalDate.now().minusMonths(1),
            expiryDate = cocaCola.calculateExpiryDate(LocalDate.now().minusMonths(1)),
            quantity = 12
        )

        // Slot 3: Monster Energy
        val monsterBatch = ProductionBatch(
            productId = monster.productId,
            manufacturingLocation = Location.VELACHERY,
            manufacturingDate = LocalDate.now().minusMonths(3),
            expiryDate = monster.calculateExpiryDate(LocalDate.now().minusMonths(3)),
            quantity = 8
        )

        // ── Vending Machine ────────────────────────────────────────────────────
        // slot3 is the firstSlot (passed to VendingMachine constructor)
        val slot3 = Slot(
            vendingMachineId = generator.IDGenerator.peekNextVendingMachineId(),
            batches = mutableListOf(monsterBatch)
        )
        val machine = VendingMachine(
            vendingMachineLocation = Location.OMR,
            establishedOn = LocalDate.now(),
            productTypeInside = ProductCategory.FOOD,
            firstSlot = slot3
        )

        // Add remaining slots
        val slot1 = Slot(machine.vendingMachineId, mutableListOf(laysBatch1, laysBatch2))
        val slot2 = Slot(machine.vendingMachineId, mutableListOf(bisleriBatch, colaOldBatch))
        machine.addSlot(slot1)
        machine.addSlot(slot2)

        machine.drawer.add(IndianCurrency.FIVE_HUNDRED, 10)
        machine.drawer.add(IndianCurrency.HUNDRED, 20)
        machine.drawer.add(IndianCurrency.FIFTY, 20)
        machine.drawer.add(IndianCurrency.TWENTY, 30)
        machine.drawer.add(IndianCurrency.TEN, 50)
        machine.drawer.add(IndianCurrency.FIVE, 50)
        machine.drawer.add(IndianCurrency.TWO, 50)
        machine.drawer.add(IndianCurrency.ONE, 100)

        VendingMachineRepository.add(machine)

        // ── Sample purchase ────────────────────────────────────────────────────
        val purchase = Purchase(
            itemsPurchased = mapOf(lays.productId to 2, bisleri.productId to 1),
            totalAmount = BigDecimal("55"),
            moneyPaidByCustomer = BigDecimal("100"),
            changeReturned = BigDecimal("45")
        )
        PurchaseRepository.add(purchase)

        println("Sample data loaded: 1 admin, 1 vending machine (${machine.vendingMachineId}), 5 food products, 3 slots.")
    }
}