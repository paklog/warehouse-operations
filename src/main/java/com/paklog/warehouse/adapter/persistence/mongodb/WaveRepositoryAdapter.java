package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.wave.Wave;
import com.paklog.warehouse.domain.wave.WaveId;
import com.paklog.warehouse.domain.wave.WaveRepository;
import com.paklog.warehouse.domain.wave.WaveStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WaveRepositoryAdapter implements WaveRepository {

    private final SpringWaveRepository springWaveRepository;

    @Autowired
    public WaveRepositoryAdapter(SpringWaveRepository springWaveRepository) {
        this.springWaveRepository = springWaveRepository;
    }

    @Override
    public void save(Wave wave) {
        WaveDocument document = new WaveDocument(wave);
        springWaveRepository.save(document);
    }

    @Override
    public Optional<Wave> findById(WaveId waveId) {
        Optional<WaveDocument> document = springWaveRepository.findById(waveId.getValue().toString());
        return document.map(WaveDocument::toDomain);
    }

    @Override
    public List<Wave> findByStatus(WaveStatus status) {
        List<WaveDocument> documents = springWaveRepository.findByStatus(status);
        return documents.stream()
                .map(WaveDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wave> findByPlannedDate(Instant plannedDate) {
        Date date = plannedDate != null ? Date.from(plannedDate) : null;
        List<WaveDocument> documents = springWaveRepository.findByPlannedDateBetween(date, date);
        return documents.stream()
                .map(WaveDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wave> findByCutoffTimeBefore(Instant cutoffTime) {
        Date date = cutoffTime != null ? Date.from(cutoffTime) : null;
        List<WaveDocument> documents = springWaveRepository.findByCutoffTimeBefore(date);
        return documents.stream()
                .map(WaveDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Wave> findByOrderId(OrderId orderId) {
        Optional<WaveDocument> document = springWaveRepository.findByOrderId(orderId.getValue().toString());
        return document.map(WaveDocument::toDomain);
    }

    @Override
    public List<Wave> findByCarrier(String carrier) {
        List<WaveDocument> documents = springWaveRepository.findByCarrier(carrier);
        return documents.stream()
                .map(WaveDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wave> findByReleaseDateBetween(Instant start, Instant end) {
        Date startDate = start != null ? Date.from(start) : null;
        Date endDate = end != null ? Date.from(end) : null;
        List<WaveDocument> documents = springWaveRepository.findByReleaseDateBetween(startDate, endDate);
        return documents.stream()
                .map(WaveDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(WaveId waveId) {
        springWaveRepository.deleteById(waveId.getValue().toString());
    }

    @Override
    public boolean existsById(WaveId waveId) {
        return springWaveRepository.existsById(waveId.getValue().toString());
    }

    @Override
    public long count() {
        return springWaveRepository.count();
    }

    @Override
    public long countByStatus(WaveStatus status) {
        return springWaveRepository.countByStatus(status);
    }
}