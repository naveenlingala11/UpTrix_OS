package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.payroll.TaxSlabDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.payroll.TaxSlab;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.payroll.TaxSlabRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaxSlabService {

    private final TaxSlabRepository taxSlabRepository;
    private final CompanyRepository companyRepository;

    public TaxSlabService(TaxSlabRepository taxSlabRepository,
                          CompanyRepository companyRepository) {
        this.taxSlabRepository = taxSlabRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSlabs(Long companyId) {
        if (companyId == null) return false;
        return taxSlabRepository.existsByCompanyIdAndActiveTrue(companyId);
    }

    /**
     * Calculate annual tax for given company + taxable annual income
     * using progressive slabs.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAnnualTax(Long companyId, BigDecimal taxableAnnualIncome) {
        if (companyId == null || taxableAnnualIncome == null) {
            return BigDecimal.ZERO;
        }

        List<TaxSlab> slabs =
                taxSlabRepository.findByCompanyIdAndActiveTrueOrderByFromAmountAsc(companyId);
        if (slabs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal income = taxableAnnualIncome;
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalTax = BigDecimal.ZERO;

        for (TaxSlab slab : slabs) {
            BigDecimal from = slab.getFromAmount() != null ? slab.getFromAmount() : BigDecimal.ZERO;
            BigDecimal to = slab.getToAmount(); // may be null
            Double rate = slab.getRatePercent() != null ? slab.getRatePercent() : 0.0;

            if (income.compareTo(from) <= 0) {
                break;
            }

            BigDecimal upper = (to != null && to.compareTo(BigDecimal.ZERO) > 0) ? to : income;
            BigDecimal taxableBand = upper.subtract(from);
            if (taxableBand.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // If income is less than upper bound, only tax until income
            if (income.compareTo(upper) < 0) {
                taxableBand = income.subtract(from);
            }

            BigDecimal bandTax = taxableBand
                    .multiply(BigDecimal.valueOf(rate))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (slab.getFixedAmount() != null) {
                bandTax = bandTax.add(slab.getFixedAmount());
            }

            totalTax = totalTax.add(bandTax);

            if (to == null || income.compareTo(to) <= 0) {
                break;
            }
        }

        return totalTax.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateMonthlyTds(Long companyId, BigDecimal taxableAnnualIncome) {
        BigDecimal annualTax = calculateAnnualTax(companyId, taxableAnnualIncome);
        if (annualTax.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return annualTax
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<TaxSlabDto> listForCompany(Long companyId) {
        return taxSlabRepository.findByCompanyIdAndActiveTrueOrderByFromAmountAsc(companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaxSlabDto save(TaxSlabDto dto) {
        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("companyId is required");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        TaxSlab entity;
        if (dto.getId() != null) {
            entity = taxSlabRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("TaxSlab not found"));
        } else {
            entity = new TaxSlab();
        }

        entity.setCompany(company);
        entity.setFromAmount(dto.getFromAmount());
        entity.setToAmount(dto.getToAmount());
        entity.setRatePercent(dto.getRatePercent());
        entity.setFixedAmount(dto.getFixedAmount());
        entity.setSortOrder(dto.getSortOrder());
        entity.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);

        TaxSlab saved = taxSlabRepository.save(entity);
        return toDto(saved);
    }

    private TaxSlabDto toDto(TaxSlab e) {
        TaxSlabDto dto = new TaxSlabDto();
        dto.setId(e.getId());
        dto.setCompanyId(e.getCompany() != null ? e.getCompany().getId() : null);
        dto.setFromAmount(e.getFromAmount());
        dto.setToAmount(e.getToAmount());
        dto.setRatePercent(e.getRatePercent());
        dto.setFixedAmount(e.getFixedAmount());
        dto.setSortOrder(e.getSortOrder());
        dto.setActive(e.getActive());
        return dto;
    }
}
