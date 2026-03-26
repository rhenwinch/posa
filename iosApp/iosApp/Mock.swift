//
//  Mock.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import shared

private let baseBreeds: [CatBreed] = [
    CatBreed(
        id: "bsho",
        name: "British Shorthair",
        altName: "Highlander, Highland Straight, Britannica",
        imageId: "s4wQfYoEk",
        origin: "United Kingdom",
        description: "The British Shorthair is a very pleasant cat to have as a companion, and is easy going and placid. The British is a fiercely loyal, loving cat and will attach herself to every one of her family members.",
        lifeSpan: "12 - 17",
        weight: "5 - 9",
        temperaments: ["Affectionate", "Easy Going", "Gentle", "Loyal", "Patient", "Calm"],
        traits: CatTraits(
            adaptability: 5, affectionLevel: 4, childFriendly: 4, dogFriendly: 5,
            energyLevel: 2, grooming: 2, healthIssues: 2, intelligence: 3,
            sheddingLevel: 4, socialNeeds: 3, strangerFriendly: 2, vocalisation: 1
        ),
        badges: CatBadges(isIndoor: false, isHypoallergenic: false, isHairless: false, hasShortLegs: false, isLap: true)
    ),
    CatBreed(
        id: "crex",
        name: "Cornish Rex",
        altName: nil,
        imageId: "unX21IBVB",
        origin: "United Kingdom",
        description: "This is a confident cat who loves people and will follow them around, waiting for any opportunity to sit in a lap or give a kiss. The Cornish Rex stays in kitten mode most of their lives and well into their senior years.",
        lifeSpan: "11 - 14",
        weight: "2 - 4",
        temperaments: ["Affectionate", "Intelligent", "Active", "Curious", "Playful"],
        traits: CatTraits(
            adaptability: 5, affectionLevel: 5, childFriendly: 4, dogFriendly: 5,
            energyLevel: 5, grooming: 1, healthIssues: 2, intelligence: 5,
            sheddingLevel: 1, socialNeeds: 5, strangerFriendly: 3, vocalisation: 1
        ),
        badges: CatBadges(isIndoor: false, isHypoallergenic: true, isHairless: false, hasShortLegs: false, isLap: true)
    ),
    CatBreed(
        id: "toyg",
        name: "Toyger",
        altName: nil,
        imageId: "O3F3_S1XN",
        origin: "United States",
        description: "The Toyger has a sweet, calm personality and is generally friendly. He's outgoing enough to walk on a leash, energetic enough to play fetch and other interactive games, and confident enough to get along with other cats and friendly dogs.",
        lifeSpan: "12 - 15",
        weight: "3 - 7",
        temperaments: ["Playful", "Social", "Intelligent"],
        traits: CatTraits(
            adaptability: 5, affectionLevel: 5, childFriendly: 4, dogFriendly: 5,
            energyLevel: 5, grooming: 1, healthIssues: 2, intelligence: 5,
            sheddingLevel: 3, socialNeeds: 3, strangerFriendly: 5, vocalisation: 5
        ),
        badges: CatBadges(isIndoor: false, isHypoallergenic: false, isHairless: false, hasShortLegs: false, isLap: true)
    ),
]

let mockCatBreeds: [CatBreed] = (0..<7).flatMap { cycle in
    baseBreeds.map { breed in
        guard cycle > 0 else { return breed }
        return CatBreed(
            id: "\(breed.id)_\(cycle)",
            name: breed.name,
            altName: breed.altName,
            imageId: breed.imageId,
            origin: breed.origin,
            description: breed.description,
            lifeSpan: breed.lifeSpan,
            weight: breed.weight,
            temperaments: breed.temperaments,
            traits: breed.traits,
            badges: breed.badges
        )
    }
}
