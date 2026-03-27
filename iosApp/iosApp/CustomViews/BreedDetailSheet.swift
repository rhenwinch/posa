//
//  BreedDetailSheet.swift
//  iosApp
//
//  Created by Rhen on 3/27/26.
//

import SwiftUI
import shared
import NukeUI


struct BreedDetailSheet: View {
    let breed: CatBreed

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                HeroImage(imageUrl: breed.imageUrl)

                VStack(alignment: .leading, spacing: 16) {
                    BreedHeaderSection(breed: breed)
                    TemperamentsSection(temperaments: breed.temperaments)
                    Text(breed.description_)
                        .font(.caption)
                        .foregroundStyle(Color.appOnSurfaceVariant)
                    Divider()
                    TraitsSection(traits: breed.traits)
                    Divider()
                    BadgesSection(badges: breed.badges)
                }
                .padding(20)
            }
        }
        .overlay(alignment: .top) {
            Rectangle()
                .frame(width: 32, height: 4)
                .roundedBorder(Color.appOnSurfaceVariant, cornerRadius: 20)
                .foregroundStyle(Color.appOnSurfaceVariant)
                .padding(.top, 20)
        }
        .background(Color.appSurfaceContainerLow)
        .ignoresSafeArea(edges: .top)
    }
}

// MARK: - Hero

private struct HeroImage: View {
    let imageUrl: String

    var body: some View {
        LazyImage(url: URL(string: imageUrl)) { state in
            if let image = state.image {
                image.centerCropped()
            } else {
                Rectangle().fill(Color.appSurfaceContainerLowest)
            }
        }
        .overlay {
            LinearGradient(
                stops: [
                    .init(color: Color.clear, location: 0),
                    .init(color: Color.appSurfaceContainerLow.opacity(0.8), location: 0.8),
                    .init(color: Color.appSurfaceContainerLow.opacity(1.0), location: 1),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .frame(height: 260)
        .clipped()
    }
}

// MARK: - Header

private struct BreedHeaderSection: View {
    let breed: CatBreed

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(breed.name)
                .font(.title2).fontWeight(.medium)
                .foregroundStyle(Color.appOnSurface)

            Text("\(breed.origin) · \(breed.lifeSpan) yrs · \(breed.weight) kg")
                .font(.caption)
                .foregroundStyle(Color.appOnSurfaceVariant)
        }
    }
}

// MARK: - Temperaments

private struct TemperamentsSection: View {
    let temperaments: [String]

    var body: some View {
        FlowLayout(spacing: 6) {
            ForEach(temperaments, id: \.self) { tag in
                Text(tag)
                    .font(.caption)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color.clear)
                    .clipShape(Capsule())
                    .overlay(Capsule().strokeBorder(Color.appOnSurfaceVariant, lineWidth: 0.5))
                    .foregroundStyle(Color.appOnSurface)
            }
        }
    }
}

// MARK: - Traits

private struct TraitsSection: View {
    let traits: CatTraits

    private var rows: [(String, Int32)] {[
        ("Adaptability",   traits.adaptability),
        ("Affection",      traits.affectionLevel),
        ("Child friendly", traits.childFriendly),
        ("Dog friendly",   traits.dogFriendly),
        ("Energy level",   traits.energyLevel),
        ("Intelligence",   traits.intelligence),
        ("Shedding",       traits.sheddingLevel),
        ("Vocalisation",   traits.vocalisation),
    ]}

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel("Traits")
            ForEach(rows, id: \.0) { name, value in
                TraitRow(name: name, value: value)
            }
        }
    }
}

private struct TraitRow: View {
    let name: String
    let value: Int32

    var body: some View {
        HStack(spacing: 10) {
            Text(name)
                .font(.caption)
                .foregroundStyle(Color.appOnSurfaceVariant)
                .frame(width: 110, alignment: .leading)

            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(Color.appSurfaceVariant)
                    Capsule()
                        .fill(Color.appOnSurface)
                        .frame(width: geo.size.width * CGFloat(value) / 5.0)
                }
            }
            .frame(height: 6)

            Text("\(value)")
                .font(.caption)
                .foregroundStyle(Color.appOnSurfaceVariant.opacity(0.5))
                .frame(width: 16, alignment: .trailing)
        }
    }
}

// MARK: - Badges

private struct BadgesSection: View {
    let badges: CatBadges

    private var items: [(String, Bool)] {[
        ("Lap cat",         badges.isLap),
        ("Hypoallergenic",  badges.isHypoallergenic),
        ("Hairless",        badges.isHairless),
        ("Short legs",      badges.hasShortLegs),
        ("Indoor",          badges.isIndoor),
    ]}

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel("Badges")
            FlowLayout(spacing: 6) {
                ForEach(items, id: \.0) { label, active in
                    BadgePill(label: label, active: active)
                }
            }
        }
    }
}

private struct BadgePill: View {
    let label: String
    let active: Bool

    var body: some View {
        HStack(spacing: 5) {
            Circle()
                .fill(Color.appOnSurface)
                .frame(width: 6, height: 6)
            Text(label)
                .font(.caption)
                .foregroundStyle(Color.appOnSurface)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(active ? Color.appSurfaceVariant : Color.clear)
        .clipShape(Capsule())
        .overlay(Capsule().strokeBorder(Color.appSurfaceVariant, lineWidth: 1))
    }
}

// MARK: - Helpers

private struct SectionLabel: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 11, weight: .medium))
            .tracking(1)
            .foregroundStyle(.tertiary)
    }
}
